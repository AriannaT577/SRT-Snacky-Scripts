// Packages & Imports
package org.firstinspires.ftc.teamcode.teamCode;
import static java.lang.Math.abs;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.IMU;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
public class LARCode extends LinearOpMode {

    // State Variables
    public double ViperPower = 0;
    public double vServoPower = 0;
    public Servo LClaw;
    public Servo RClaw;
    public CRServo CRot;
    public Servo LRot;
    public Servo RRot;
    public CRServo vServo;

    @Override
    public void runOpMode() throws InterruptedException {
        
        // Classify Variables
        DcMotor FL = hardwareMap.dcMotor.get("FL");
        DcMotor BL = hardwareMap.dcMotor.get("BL");
        DcMotor FR = hardwareMap.dcMotor.get("FR");
        DcMotor BR = hardwareMap.dcMotor.get("BR");

        DcMotor VL = hardwareMap.dcMotor.get("VL");
        DcMotor VR = hardwareMap.dcMotor.get("VR");

        LClaw = hardwareMap.get(Servo.class, "LClaw");
        RClaw = hardwareMap.get(Servo.class, "RClaw");

        CRot = hardwareMap.get(CRServo.class, "CRot");
        CRot.resetDeviceConfigurationForOpMode();
        LRot = hardwareMap.get(Servo.class, "LRot");
        RRot = hardwareMap.get(Servo.class, "RRot");

        vServo = hardwareMap.get(CRServo.class, "vServo");

        double mandPower = 0;
        LClaw.setPosition(0.18);
        RClaw.setPosition(0);
        
        // Set Variable Direction
        FR.setDirection(DcMotorSimple.Direction.FORWARD);
        BR.setDirection(DcMotorSimple.Direction.REVERSE);
        FL.setDirection(DcMotorSimple.Direction.FORWARD);
        BL.setDirection(DcMotorSimple.Direction.REVERSE);

        VR.setDirection(DcMotorSimple.Direction.REVERSE);
        
        RClaw.setDirection(Servo.Direction.FORWARD);
        LClaw.setDirection(Servo.Direction.FORWARD);
        
        IMU imu = hardwareMap.get(IMU.class, "imu");
        
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
            RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD));
        
        imu.initialize(parameters);

        FR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {

            // Movement Through Sticks
            double y = -gamepad1.left_stick_x;
            double x = gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;
            
            if (gamepad1.start){
                imu.resetYaw();
            }
            
            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            
            double rotX = x * Math.cos(-botHeading) - y * Math.cos(-botHeading);
            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);
            
            rotX = rotX * 1.1;
            
            double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);

            FL.setPower((rotY + rotX + rx) / denominator);
            BL.setPower((rotY - rotX + rx)/ denominator);
            FR.setPower((rotY - rotX - rx)/ denominator);
            BR.setPower((rotY + rotX - rx)/ denominator);



            // Viper Slides (Vertical)
            //if (gamepad2.dpad_up){ // Increase Viper Height
             //   ViperPower = -1;
            //}
            //else if(gamepad2.dpad_down){ // Decrease Viper Height
            //    ViperPower = 1;
            //}
            //else if (!gamepad2.dpad_down && !gamepad2.dpad_up){ // Keep Current Viper Height
             //   ViperPower = -0.2;
            //}
            double ViperPower = gamepad2.left_stick_y; // Not Inverted
            if (abs(ViperPower) <= 0.3){ 
                ViperPower = -0.2;
            }
            
            
            // Head Rotation (Horizontal)
            if(gamepad2.dpad_right){
                CRot.setPower(-1); // Rotate Right
            }
            else if(gamepad2.dpad_left){ // Rotate Left
                CRot.setPower(1);
            }
            else{ // Stop Rotation
                CRot.setPower(0);
            }



            // Head Rotation (Vertical)
            if (gamepad2.y){ // Rotate Up
                RRot.setPosition(0.75);
            }
            else if (gamepad2.x){ // Rotate Down
                RRot.setPosition(0.40);
            }
            // Head Extend (beltServo)
            double vServoPower = -gamepad2.right_stick_y; // Not Inverted
            
            
            // Claws
            if (gamepad2.right_bumper){ // Close Claw
                RClaw.setPosition(0.18); // more positive --> more grippy (before 1, now 0.35)
                LClaw.setPosition(0); // more positive --> more grippy (before 1, now 0.68)
            }
            else if(gamepad2.left_bumper){ // Open Claw
                RClaw.setPosition(0); // more negative --> less grippy
                LClaw.setPosition(0.18); // more negative --> less grippy
            }
            
            if (gamepad2.dpad_up){
                RClaw.setDirection(Servo.Direction.REVERSE);
                LClaw.setDirection(Servo.Direction.REVERSE);
            } else if (gamepad2.dpad_down){
                RClaw.setDirection(Servo.Direction.FORWARD);
                LClaw.setDirection(Servo.Direction.FORWARD);
            }

            VL.setPower(ViperPower);
            VR.setPower(ViperPower);
            vServo.setPower(vServoPower);
            
            // Update Pos of Vr
            double vrpos = VR.getCurrentPosition();
            double vlpos = VL.getCurrentPosition();

            telemetry.addData("VrPOS:", vrpos);
            telemetry.addData("VlPOS:", vlpos);
            telemetry.update();
        }
    }
}
