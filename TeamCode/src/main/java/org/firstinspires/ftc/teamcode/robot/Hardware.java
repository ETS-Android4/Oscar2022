package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;


public class Hardware {
    HardwareMap hwMap = null;
    public Drivetrain dt = null;
    public Intake intake = null;
    public Grabber grabber = null;
    public Elbow elbow = null;
    private Pose2d vel;
    public LinearSlides slides = null;
    public Flippers flippers = null;

    public SampleMecanumDrive drive;

    private double poseHeading = 0;
    private double poseX = 0;
    private double poseY = 0;

    public Hardware(HardwareMap hardwareMap){
        hwMap = hardwareMap;
        dt = new Drivetrain(hwMap);
        intake = new Intake(hwMap);
        grabber = new Grabber(hwMap);
        elbow = new Elbow(hwMap);
        slides = new LinearSlides(hwMap);
        flippers = new Flippers(hwMap);


        drive = new SampleMecanumDrive(hwMap);
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        vel = new Pose2d(0,0,0);
        elbow.moveStart();
        grabber.goStart();
        grabber.closeGrab();

    }


    public Hardware() {

    }

    //Inits hardware for opmode
    public void init(HardwareMap ahwMap){
        //ahwMap is hwMap
        hwMap = ahwMap;
        dt = new Drivetrain(hwMap);
        intake = new Intake(hwMap);
        grabber = new Grabber(hwMap);
        elbow = new Elbow(hwMap);
        slides = new LinearSlides(hwMap);
        flippers = new Flippers(hwMap);


        drive = new SampleMecanumDrive(hwMap);
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        vel = new Pose2d(0,0,0);
        elbow.moveStart();
        grabber.goStart();
        grabber.closeGrab();

    }
    //too lazy to make a new class( vel method will be here)

    public Pose2d getVel(){return vel;}
    public void setVel(Pose2d v){vel = v;}
    public void updateX(double X) {poseX = X;}
    public void updateY(double Y) {poseY = Y;}
    public void updateHeading(double heading) {poseHeading = heading;}
    public double getPoseX() {return(poseX);}
    public double getPoseY() {return(poseY);}
    public double getPoseHeading() {return(poseHeading);}
}
