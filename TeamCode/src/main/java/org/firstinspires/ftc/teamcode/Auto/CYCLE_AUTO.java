package org.firstinspires.ftc.teamcode.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.robot.Robot;

import org.firstinspires.ftc.teamcode.robot.Hardware;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

import dashboard.RobotConstants;

@Config
@Autonomous(group = "drive")
public class CYCLE_AUTO extends LinearOpMode {
    private FtcDashboard dashboard;
    public static Pose2d startPR = new Pose2d(RobotConstants.STARTX,RobotConstants.STARTY,Math.toRadians(RobotConstants.HEADING));
    public static Pose2d deliverPos = new Pose2d(RobotConstants.DELIVERPOSX,RobotConstants.DELIVERPOSY,RobotConstants.DELIVERPOSANG);
    Hardware Oscar = new Hardware();

    @Override
    public void runOpMode() throws InterruptedException {

        Pose2d startPose = new Pose2d(RobotConstants.STARTPOSEX, RobotConstants.STARTPOSEY, RobotConstants.STARTPOSEANG);
        Oscar.init(hardwareMap);
        TrajectorySequence autoTrajectory1 = Oscar.drive.trajectorySequenceBuilder(startPR)

                .back(65)
                .build();

        TrajectorySequence autoTrajectory2 = Oscar.drive.trajectorySequenceBuilder(autoTrajectory1.end())
                .forward(65)
                .build();

        TrajectorySequence autoTrajectory3 = Oscar.drive.trajectorySequenceBuilder(autoTrajectory2.end())
                .back(65)
                .build();
        TrajectorySequence autoTrajectory4 = Oscar.drive.trajectorySequenceBuilder(autoTrajectory3.end())
                .forward(65)
                .build();

        TrajectorySequence autoTrajectory5 = Oscar.drive.trajectorySequenceBuilder(autoTrajectory4.end())
                .back(65)
                .build();


        waitForStart();
        Oscar.drive.setPoseEstimate(startPR);

        // When auto starts
        Oscar.elbow.goToGrabPos();
        Thread.sleep(250);

        Oscar.grabber.closeGrab();

        Oscar.intake.reverse();

        Thread.sleep(100);

        Oscar.elbow.moveStart();

        Oscar.slides.slidesHome();

        Oscar.slides.slidesTop();

        Thread.sleep(1000);

        Oscar.elbow.moveTop();

        Oscar.grabber.goTop();

        Oscar.grabber.grabberGrabExtra();

        Thread.sleep(600);

        Oscar.grabber.openGrab();

        Thread.sleep(600);

        Oscar.elbow.moveStart();

        Thread.sleep(500);

        Oscar.grabber.goStart();

        Oscar.grabber.grabberGrabExtra();

        Oscar.slides.slidesGrab();

        Thread.sleep(650);

        Oscar.elbow.goToGrabPos();

        Oscar.grabber.openGrab();

        Oscar.intake.on();

        Oscar.drive.followTrajectorySequence(autoTrajectory1);

        Thread.sleep(750);

        Oscar.drive.followTrajectorySequence(autoTrajectory2);

            Oscar.grabber.closeGrab();

            Oscar.intake.reverse();
            Thread.sleep(100);

            Oscar.elbow.moveStart();

            Thread.sleep(250);
            Oscar.slides.slidesTop();

            Thread.sleep(1000);
            Oscar.elbow.moveTop();

            Oscar.grabber.goTop();

            Oscar.grabber.grabberGrabExtra();

            Oscar.grabber.openGrab();

            Thread.sleep(500);

            Oscar.elbow.moveStart();
            Thread.sleep(500);
            Oscar.grabber.goStart();
            Oscar.grabber.grabberGrabExtra();

            Oscar.slides.slidesGrab();
            Thread.sleep(600);

            Oscar.elbow.goToGrabPos();

            Oscar.intake.on();

            Oscar.drive.followTrajectorySequence(autoTrajectory3);

            Thread.sleep(750);

            Oscar.drive.followTrajectorySequence(autoTrajectory4);

            Oscar.grabber.closeGrab();

            Oscar.intake.reverse();

            Thread.sleep(100);

            Oscar.elbow.moveStart();

            Thread.sleep(250);

            Oscar.slides.slidesTop();
            Thread.sleep(1000);

            Oscar.elbow.moveTop();
            Oscar.grabber.goTop();
            Oscar.grabber.grabberGrabExtra();
            Oscar.grabber.openGrab();

            Thread.sleep(500);

            Oscar.elbow.moveStart();
            Thread.sleep(500);
            Oscar.grabber.goStart();
            Oscar.grabber.grabberGrabExtra();
            Oscar.slides.slidesGrab();
            Thread.sleep(600);

            Oscar.elbow.goToGrabPos();

            Oscar.intake.on();

            Oscar.drive.followTrajectorySequence(autoTrajectory5);

            Thread.sleep(1000);

            Oscar.intake.off();

    }


}
