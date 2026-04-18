package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Random;

@TeleOp
public class rgbColourRotatorThing extends LinearOpMode {

    private CRServo conv1;
    private CRServo conv2;
    private Servo ShootAngle;
    private Servo ShootHelp;
    private Servo CBLOCK;
    private Servo led;

    // Strafe correction factors
    private double leftStrafeCorrectionFactor = -0.005;
    private double rightStrafeCorrectionFactor = -0.01;

    // Toggle button state tracking
    private boolean prevLeftBumper = false;
    private boolean prevRightBumper = false;
    private boolean prevBack = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadRight = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;

    // Toggle power storage
    private double conveyorTogglePower = 0;
    private double intakeTogglePower = 0;
    private boolean cblockClosed = false;
    
    private boolean shift = false;

    //colour stuff
    private String[] colors = {"Red","Orange","Yellow","Sage","Green","Azure","Blue","Violet","White"};
    private double[] colorPositions = {0.277, 0.333, 0.388, 0.444, 0.5, 0.555, 0.611, 0.722, 1.0};
    private Random random = new Random();
    private int actionValue = 0;
    private int lastColorIndex = 0;  

    private void pickRandomColor() {
        actionValue++;
        lastColorIndex = random.nextInt(colors.length);
        led.setPosition(colorPositions[lastColorIndex]);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        
        double DriveSpeed = 1;
        double ShooterPower = 0;
        double turnspeed = 0.5;
        

        // Hardware mapping
        DcMotor backRight = hardwareMap.dcMotor.get("black");
        DcMotor frontRight = hardwareMap.dcMotor.get("green");
        DcMotor backLeft = hardwareMap.dcMotor.get("orange");
        DcMotor frontLeft = hardwareMap.dcMotor.get("blue");
        DcMotor RShooter = hardwareMap.dcMotor.get("brown");
        DcMotor LShooter = hardwareMap.dcMotor.get("pink");
        DcMotor Lintake = hardwareMap.dcMotor.get("teal");
        DcMotor Rintake = hardwareMap.dcMotor.get("gray");

        conv1 = hardwareMap.get(CRServo.class, "conv1");
        conv2 = hardwareMap.get(CRServo.class, "conv2");
        ShootAngle = hardwareMap.get(Servo.class, "ShootAngle");
        ShootHelp = hardwareMap.get(Servo.class, "ShootHelp");
        CBLOCK = hardwareMap.get(Servo.class, "servo");
        led = hardwareMap.get(Servo.class, "led");
        
        // Motor directions
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        LShooter.setDirection(DcMotorSimple.Direction.REVERSE);
        Lintake.setDirection(DcMotorSimple.Direction.REVERSE);
        conv2.setDirection(DcMotorSimple.Direction.FORWARD);
        ShootHelp.setDirection(Servo.Direction.REVERSE);

        // Zero power behavior
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        
        //ShootHelp.setPosition(0);
        //CBLOCK.setPosition(0);  // CBLOCK initial position

        waitForStart();
        if (isStopRequested()) return;

        while (opModeIsActive()) {

            // MOVEMENT INPUT
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x * 1.1;
            double rx = gamepad1.right_stick_x * turnspeed;

            // Strafe correction
            double strafeCorrection;
            if (x > 0) strafeCorrection = -Math.pow(x, 2) * leftStrafeCorrectionFactor;
            else if (x < 0) strafeCorrection = Math.pow(x, 2) * rightStrafeCorrectionFactor;
            else strafeCorrection = 0;

            rx += strafeCorrection;

            // SPEED MODES
            if (gamepad1.right_trigger > 0) { DriveSpeed = 1; turnspeed = 0.5;}
            if (gamepad1.b) { DriveSpeed = 0.5; turnspeed = 0.8; pickRandomColor();}
            if (gamepad1.left_trigger > 0) { DriveSpeed = 0.2; turnspeed = 0.9;}
            

            // WHEEL POWER CALC
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double fl = (y + x + rx) / denominator * DriveSpeed;
            double bl = (y - x + rx) / denominator * DriveSpeed;
            double fr = (y - x - rx) / denominator * DriveSpeed;
            double br = (y + x - rx) / denominator * DriveSpeed;

            frontLeft.setPower(fl);
            backLeft.setPower(bl);
            frontRight.setPower(fr);
            backRight.setPower(br);


            // SHOOTER ANGLE + SPEED COMBINED
            if (gamepad2.dpad_up && !prevDpadUp) {
                ShootAngle.setPosition(0.63);
                ShooterPower = 0.85;
                pickRandomColor();
            }
            if (gamepad2.dpad_right && !prevDpadRight) {
                ShootAngle.setPosition(0.67);
                ShooterPower = 0.4;
                pickRandomColor();
            }
            if (gamepad2.dpad_down && !prevDpadDown) {
                ShootAngle.setPosition(0.7);
                ShooterPower = 0.6;
                pickRandomColor();
            }
            if (gamepad2.dpad_left && !prevDpadLeft) {
                ShootAngle.setPosition(0.8);
                ShooterPower = 0.7;
                pickRandomColor();
            }

            prevDpadUp = gamepad2.dpad_up;
            prevDpadRight = gamepad2.dpad_right;
            prevDpadDown = gamepad2.dpad_down;
            prevDpadLeft = gamepad2.dpad_left;

            RShooter.setPower(ShooterPower);
            LShooter.setPower(ShooterPower);
            //=========================================================
            //                  **CONVEYOR SYSTEM**
            //=========================================================

            // Toggle with left bumper
            if (gamepad2.left_bumper && !prevLeftBumper) {
                conveyorTogglePower = (conveyorTogglePower == 0) ? 1 : 0;
                pickRandomColor();
            }
            prevLeftBumper = gamepad2.left_bumper;

            // Trigger override (reverse)
            double conveyorOutput;
            if (gamepad2.left_trigger > 0.05) {
                conveyorOutput = -0.5;
            } else {
                conveyorOutput = conveyorTogglePower;
            }

            //=========================================================
            //                   **INTAKE SYSTEM**
            //=========================================================

            // Toggle with right bumper
            if (gamepad2.right_bumper && !prevRightBumper) {
                intakeTogglePower = (intakeTogglePower == 0) ? 1 : 0;
                pickRandomColor();
            }
            prevRightBumper = gamepad2.right_bumper;

            // Trigger override (reverse)
            double intakeOutput;
            if (gamepad2.right_trigger > 0.05) {
                intakeOutput = -0.5;
            } else {
                intakeOutput = intakeTogglePower;
            }

            // APPLY POWER (must be at bottom!)
            conv1.setPower(conveyorOutput);
            conv2.setPower(conveyorOutput);
            Lintake.setPower(intakeOutput);
            Rintake.setPower(intakeOutput);

            //=========================================================
            //                   **CBLOCK TOGGLE**
            //=========================================================

            if (gamepad2.back && !prevBack) {
                cblockClosed = !cblockClosed;
                pickRandomColor();
            }
            prevBack = gamepad2.back;

            if (cblockClosed) {
                CBLOCK.setPosition(1);
            } else {
                CBLOCK.setPosition(0);
            }

          

            // TELEMETRY
            telemetry.addData("FL", fl);
            telemetry.addData("FR", fr);
            telemetry.addData("BL", bl);
            telemetry.addData("BR", br);
            telemetry.addData("DriveSpeed", DriveSpeed);
            telemetry.addData("Shooter Power", ShooterPower);
            telemetry.addData("ShootAngle", ShootAngle.getPosition());
            telemetry.addData("Conveyor Output", conveyorOutput);
            telemetry.addData("Intake Output", intakeOutput);
            telemetry.addData("ShootHelper", ShootHelp.getPosition());
            telemetry.addData("CBlock Closed", cblockClosed);  // CBLOCK telemetry
            telemetry.addData("RGB Color", colors[lastColorIndex] + " (" + colorPositions[lastColorIndex] + ")");
          telemetry.update();
        } 

    }
}
