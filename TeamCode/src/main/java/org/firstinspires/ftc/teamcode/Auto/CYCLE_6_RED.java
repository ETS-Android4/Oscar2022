package org.firstinspires.ftc.teamcode.Auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.robot.CV.BarcodePositionDetector;
import org.firstinspires.ftc.teamcode.robot.DEPOSIT_FSM;
import org.firstinspires.ftc.teamcode.robot.Hardware;
import org.firstinspires.ftc.teamcode.robot.INTAKE_FSM;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
@Autonomous
public class CYCLE_6_RED extends LinearOpMode {
    Hardware Oscar;

    double AMOUNT_ITERATE_Y = 2;

    double ADJUSTABLE_INTAKE_X = 48;
    double AMOUNT_INCREASE_INTAKE_X = 1.8;

    //Milliseconds
    double STUCK_INTAKE_TIMEOUT = 2000;

    Pose2d startPose = new Pose2d(7.4, 64, Math.toRadians(180));
    Pose2d depositPose = new Pose2d(21.4, 64, Math.toRadians(180));
    Pose2d bottomDepositPose = new Pose2d(-10, -64, Math.toRadians(-180));
    Pose2d warehousePose = new Pose2d(36, 64,Math.toRadians(-180));
    Pose2d intakePose = new Pose2d(ADJUSTABLE_INTAKE_X, -62, Math.toRadians(-180));
    Vector2d intakeVector = new Vector2d(ADJUSTABLE_INTAKE_X, -62);
    Vector2d warehouseVector = new Vector2d(36, 64);
    Pose2d splineTest = new Pose2d(8.6,58.2, Math.toRadians(130));

    Trajectory START_TO_DEPOSIT;
    TrajectorySequence DEPOSIT_TO_WAREHOUSE;
    TrajectorySequence WAREHOUSE_TO_DEPOSIT;
    Trajectory START_TO_DEPOSIT_BOTTOM;
    Trajectory DEPOSIT_BOTTOM_TO_WAREHOUSE;

    private void iterateIntakeX() {
        ADJUSTABLE_INTAKE_X += AMOUNT_INCREASE_INTAKE_X;
        intakeVector = new Vector2d(ADJUSTABLE_INTAKE_X, 64);
        intakePose = new Pose2d(ADJUSTABLE_INTAKE_X, 64, Math.toRadians(-180));
        DEPOSIT_TO_WAREHOUSE = Oscar.drive.trajectorySequenceBuilder(START_TO_DEPOSIT.end())
                .lineToLinearHeading(warehousePose)
                .splineToConstantHeading(intakeVector, Math.toRadians(-180))
                .build();
        WAREHOUSE_TO_DEPOSIT = Oscar.drive.trajectorySequenceBuilder(DEPOSIT_TO_WAREHOUSE.end())
                .splineToConstantHeading(warehouseVector, Math.toRadians(-180))
                .lineToLinearHeading(depositPose)
                .splineToLinearHeading(splineTest, Math.toRadians(130))
                .build();
    }

    enum STATE {
        INIT,
        BACKWARD,
        INTAKE,
        RESTART_INTAKE,
        FORWARD,
        IDLE
    }

    BarcodePositionDetector.BarcodePosition position;

    @Override
    public void runOpMode() throws InterruptedException {


        Oscar = new Hardware(hardwareMap, telemetry);
        DEPOSIT_FSM deposit_fsm = new DEPOSIT_FSM(Oscar, telemetry, gamepad1, gamepad2);
        INTAKE_FSM intake_fsm = new INTAKE_FSM(Oscar, telemetry, gamepad1, gamepad2);

        START_TO_DEPOSIT = Oscar.drive.trajectoryBuilder(startPose)
                .lineToLinearHeading(depositPose)
                .build();
        START_TO_DEPOSIT_BOTTOM = Oscar.drive.trajectoryBuilder(startPose)
                .lineToLinearHeading(bottomDepositPose)
                .build();
        DEPOSIT_BOTTOM_TO_WAREHOUSE = Oscar.drive.trajectoryBuilder(bottomDepositPose)
                .lineToLinearHeading(warehousePose)
                .splineToConstantHeading(intakeVector, Math.toRadians(180))
                .build();
        DEPOSIT_TO_WAREHOUSE = Oscar.drive.trajectorySequenceBuilder(START_TO_DEPOSIT.end())
                .lineToLinearHeading(warehousePose)
                .splineToConstantHeading(intakeVector, Math.toRadians(180))
                .build();
        WAREHOUSE_TO_DEPOSIT = Oscar.drive.trajectorySequenceBuilder(DEPOSIT_TO_WAREHOUSE.end())
                .splineToConstantHeading(warehouseVector, Math.toRadians(180))
                .lineToLinearHeading(depositPose)
                .build();

        Oscar.drive.setPoseEstimate(startPose);

        Oscar.elbow.goToGrabPos();
        Oscar.grabber.goStart();
        Oscar.grabber.openGrab();
        Oscar.slides.slidesHome();

        CYCLE_RED_CV.STATE state = CYCLE_RED_CV.STATE.INIT;

        ElapsedTime time = new ElapsedTime();
        ElapsedTime RUNTIME = new ElapsedTime();

        boolean ENSURE_ONE_DEPOSIT = false;

        double STOP_CYCLING_TIMEOUT = 26.5;

        Oscar.flippers.moveUp("front");
        Oscar.flippers.moveDown("back");

        int counterTop = 0;
        int counterBottom = 0;
        int counterMid = 0;

        Oscar.cvUtil.init();

        BarcodePositionDetector.BarcodePosition barcodePosition;

        while (!isStopRequested() && !opModeIsActive()) {
            barcodePosition = Oscar.cvUtil.getBarcodePosition();
            telemetry.addData("Barcode position", barcodePosition);
            if(barcodePosition == BarcodePositionDetector.BarcodePosition.LEFT){
                counterBottom++;
            }
            else if( barcodePosition == BarcodePositionDetector.BarcodePosition.MIDDLE){
                counterMid++;
            }
            else if( barcodePosition == BarcodePositionDetector.BarcodePosition.RIGHT){
                counterTop++;
            }

            telemetry.update();
        }


        if (counterBottom > counterMid && counterBottom > counterTop){
            position = BarcodePositionDetector.BarcodePosition.LEFT;
        }
        if(counterMid > counterTop && counterMid > counterBottom){
            position = BarcodePositionDetector.BarcodePosition.MIDDLE;

        }
        if (counterTop > counterBottom && counterTop > counterMid){
            position = BarcodePositionDetector.BarcodePosition.RIGHT;
        }

        waitForStart();

        Oscar.drive.followTrajectoryAsync(START_TO_DEPOSIT_BOTTOM);
        time.reset();
        RUNTIME.reset();

        while(isStarted() && !isStopRequested()) {
            Oscar.intake.frontOut();
            switch (state) {
                case INIT:
                    if(position == BarcodePositionDetector.BarcodePosition.LEFT) {
                        if(!ENSURE_ONE_DEPOSIT && time.milliseconds() > 500) {
                            deposit_fsm.startDepositbot = true;
                            ENSURE_ONE_DEPOSIT = true;
                        }
                        else if(deposit_fsm.THE_THING_CAN_BE_DROPPED_NOW) {
                            deposit_fsm.DROP_THE_THING_NOW = true;
                            ENSURE_ONE_DEPOSIT = false;
                            Oscar.drive.followTrajectoryAsync(DEPOSIT_BOTTOM_TO_WAREHOUSE);
                            state = CYCLE_RED_CV.STATE.BACKWARD;
                            time.reset();
                        }
                    }
                    else if(position == BarcodePositionDetector.BarcodePosition.MIDDLE) {
                        if(!ENSURE_ONE_DEPOSIT && time.milliseconds() > 500) {
                            deposit_fsm.startDepositmid = true;
                            ENSURE_ONE_DEPOSIT = true;
                        }
                        else if(deposit_fsm.THE_THING_CAN_BE_DROPPED_NOW) {
                            deposit_fsm.DROP_THE_THING_NOW = true;
                            ENSURE_ONE_DEPOSIT = false;
                            Oscar.drive.followTrajectorySequenceAsync(DEPOSIT_TO_WAREHOUSE);
                            state = CYCLE_RED_CV.STATE.BACKWARD;
                            time.reset();
                        }
                    }
                    else {
                        if(!ENSURE_ONE_DEPOSIT && time.milliseconds() > 500) {
                            deposit_fsm.startDeposittop = true;
                            ENSURE_ONE_DEPOSIT = true;
                        }
                        else if(deposit_fsm.THE_THING_CAN_BE_DROPPED_NOW) {
                            deposit_fsm.DROP_THE_THING_NOW = true;
                            ENSURE_ONE_DEPOSIT = false;
                            Oscar.drive.followTrajectorySequenceAsync(DEPOSIT_TO_WAREHOUSE);
                            state = CYCLE_RED_CV.STATE.BACKWARD;
                            time.reset();
                        }
                    }
                    break;
                case BACKWARD:
                    if(((DistanceSensor) Oscar.colorBack).getDistance(DistanceUnit.CM) < 1.5 || !Oscar.drive.isBusy()) {
                        state = CYCLE_RED_CV.STATE.INTAKE;
                        time.reset();
                    }
                    else {
                        Oscar.intake.backIn();
                    }
                    break;
                case INTAKE:
                    Oscar.drive.setWeightedDrivePower(new Pose2d(-.4,0,0));
                    if(((DistanceSensor) Oscar.colorBack).getDistance(DistanceUnit.CM) < 1.5) {
                        intake_fsm.SET_EXEC_BACK_FLIP(true);
                        Oscar.drive.setWeightedDrivePower(new Pose2d(0,0,0));
                        Oscar.drive.setPoseEstimate(new Pose2d(Oscar.drive.getPoseEstimate().getX(), Oscar.drive.getPoseEstimate().getY() + AMOUNT_ITERATE_Y, Oscar.drive.getPoseEstimate().getHeading()));
                        if(RUNTIME.seconds() < STOP_CYCLING_TIMEOUT) {
                            Oscar.drive.followTrajectorySequenceAsync(WAREHOUSE_TO_DEPOSIT);
                            state = CYCLE_RED_CV.STATE.FORWARD;
                            time.reset();
                        }
                        else state = CYCLE_RED_CV.STATE.IDLE;
                    }
                    else if(time.milliseconds() > STUCK_INTAKE_TIMEOUT) {
                        state = CYCLE_RED_CV.STATE.RESTART_INTAKE;
                        time.reset();
                    }
                    break;
                case RESTART_INTAKE:
                    if(Oscar.drive.getPoseEstimate().getX() < 52) {
                        state = CYCLE_RED_CV.STATE.INTAKE;
                        Oscar.intake.backIn();
                        time.reset();
                    }
                    else{
                        Oscar.intake.backOut();
                        Oscar.drive.setWeightedDrivePower(new Pose2d(.4,0,0));
                    }
                    break;
                case FORWARD:
                    if(((DistanceSensor) Oscar.colorBack).getDistance(DistanceUnit.CM) > 1.5 && Oscar.drive.getPoseEstimate().getX() < 18 && !ENSURE_ONE_DEPOSIT) {
                        deposit_fsm.startDeposittop = true;
                        ENSURE_ONE_DEPOSIT = true;
                    }
                    else if(deposit_fsm.THE_THING_CAN_BE_DROPPED_NOW) {
                        iterateIntakeX();
                        deposit_fsm.DROP_THE_THING_NOW = true;
                        ENSURE_ONE_DEPOSIT = false;
                        Oscar.drive.followTrajectorySequenceAsync(DEPOSIT_TO_WAREHOUSE);
                        state = CYCLE_RED_CV.STATE.BACKWARD;
                        time.reset();
                    }
                    break;
                case IDLE:
                    Oscar.intake.off();
                    break;
            }
            deposit_fsm.doDepositTopAsync();
            deposit_fsm.doDepositMiddleAsync();
            deposit_fsm.doDepositBottomAsync();
            intake_fsm.handleEvents(deposit_fsm.isAnyBusy());
            Oscar.drive.update();
        }
    }
}
