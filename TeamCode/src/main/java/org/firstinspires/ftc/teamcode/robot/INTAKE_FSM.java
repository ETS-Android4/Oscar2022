package org.firstinspires.ftc.teamcode.robot;

import android.system.Os;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class INTAKE_FSM {
    enum BACK_STATE {
        INIT,
        STATE_0,
        STATE_1,
        STATE_2,
        STATE_3
    }
    enum FRONT_STATE {
        INIT,
        STATE_0,
        STATE_1,
        STATE_2,
        STATE_3
    }
    private BACK_STATE back_state = BACK_STATE.INIT;
    private FRONT_STATE front_state = FRONT_STATE.INIT;
    private final Hardware Oscar;
    private final Gamepad gamepad1;
    private final Gamepad gamepad2;
    private boolean frontBusy = false;
    private boolean backBusy = false;
    private final Telemetry telemetry;

    private boolean EXEC_BACK_FLIP = false;
    private boolean EXEC_FRONT_FLIP = false;

    public INTAKE_FSM(Hardware hardware, Telemetry telemetry, Gamepad c1, Gamepad c2) {
        this.Oscar = hardware;
        this.telemetry = telemetry;
        gamepad1 = c1;
        gamepad2 = c2;
    }

    private final ElapsedTime time = new ElapsedTime();

    public boolean isBackBusy() {return backBusy;}
    public boolean isFrontBusy() {return frontBusy;}

    public void reset() {
        backBusy = false;
        frontBusy = false;
        back_state = BACK_STATE.INIT;
        front_state = FRONT_STATE.INIT;
        time.reset();
    }

    public void SET_EXEC_BACK_FLIP(boolean EXEC) {EXEC_BACK_FLIP = EXEC;}
    public void SET_EXEC_FRONT_FLIP(boolean EXEC) {EXEC_FRONT_FLIP = EXEC;}

    public void doFlipFrontAsync() {
        telemetry.addData("FRONT FLIPPER STATE: ", front_state);
        switch(front_state) {
            case INIT:
                if((((gamepad2.dpad_up || gamepad1.dpad_up) && !backBusy) || EXEC_FRONT_FLIP) && !LOGIC.IS_THING_IN_DA_ROBOT) {                    EXEC_FRONT_FLIP = false;
                    EXEC_FRONT_FLIP = false;
                    front_state = FRONT_STATE.STATE_0;
                    frontBusy = true;
                    time.reset();
                }
                else {
                    frontBusy = false;
                }
                break;
            case STATE_0:
                if(time.milliseconds() > 100) {
                    front_state = FRONT_STATE.STATE_1;
                    time.reset();
                }
                else {
                    Oscar.flippers.moveUp("front");
                    Oscar.intake.frontOut();
                }
                break;
            case STATE_1:
                if(time.milliseconds() > 200) {
                    front_state = FRONT_STATE.STATE_2;
                    time.reset();
                }
                else {
                    Oscar.intake.frontOut();
                }
                break;
            case STATE_2:
                if(time.milliseconds() > 1200) {
                    front_state = FRONT_STATE.STATE_3;
                    Oscar.flippers.moveDown("front");
                    time.reset();
                }
                if(((DistanceSensor) Oscar.colorFront).getDistance(DistanceUnit.CM) > 2) {
                    LOGIC.IS_THING_IN_DA_ROBOT = true;
                }
                else {
                    Oscar.intake.frontOut();
                }
                break;
            case STATE_3:
                if(time.milliseconds() > 20) {
                    front_state = FRONT_STATE.INIT;
                    if(((DistanceSensor) Oscar.colorBack).getDistance(DistanceUnit.CM) < 2) {
                        LOGIC.IS_THING_IN_DA_ROBOT = false;
                    }
                    EXEC_FRONT_FLIP = false;
                    Oscar.intake.off();
                    reset();
                }
                break;
            default:
                front_state = FRONT_STATE.INIT;
                break;
        }
    }

    public void doFlipBackAsync() {
        telemetry.addData("BACK FLIPPER STATE: ", back_state);
        switch(back_state) {
            case INIT:
                if(EXEC_BACK_FLIP && !LOGIC.IS_THING_IN_DA_ROBOT) {
                    EXEC_BACK_FLIP = false;
                    back_state = BACK_STATE.STATE_0;
                    backBusy = true;
                    time.reset();
                }
                else {
                    backBusy = false;
                }
                break;
            case STATE_0:
                if(time.milliseconds() > 100) {
                    back_state = BACK_STATE.STATE_1;
                    time.reset();
                }
                else {
                    Oscar.flippers.moveUp("back");
                    Oscar.intake.backOut();
                }
                break;
            case STATE_1:
                if(time.milliseconds() > 200) {
                    back_state = BACK_STATE.STATE_2;
                    time.reset();
                }
                else {
                    Oscar.intake.backOut();
                }
                break;
            case STATE_2:
                if(time.milliseconds() > 1200) {
                    back_state = BACK_STATE.STATE_3;
                    Oscar.flippers.moveDown("back");
                    time.reset();
                }
                if(((DistanceSensor) Oscar.colorBack).getDistance(DistanceUnit.CM) > 2) {
                    LOGIC.IS_THING_IN_DA_ROBOT = true;
                }
                else{
                    Oscar.intake.backOut();
                }
                break;
            case STATE_3:
                if(time.milliseconds() > 20) {
                    if(((DistanceSensor) Oscar.colorBack).getDistance(DistanceUnit.CM) < 2) {
                        LOGIC.IS_THING_IN_DA_ROBOT = false;
                    }
                    EXEC_BACK_FLIP = false;
                    back_state = BACK_STATE.INIT;
                    Oscar.intake.off();
                    reset();
                }
                break;
            default:
                back_state = BACK_STATE.INIT;
                break;
        }
    }
}
