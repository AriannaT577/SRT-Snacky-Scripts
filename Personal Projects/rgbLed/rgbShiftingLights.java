package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
public class rgbShiftingLights extends LinearOpMode {

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

    // Toggle power storage
    private double conveyorTogglePower = 0;
    private double intakeTogglePower = 0;
    private boolean cblockClosed = false;
    
    private boolean shift = false;


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
            if (gamepad1.right_trigger > 0) { DriveSpeed = 1; turnspeed = 0.5; }
            if (gamepad1.b) { DriveSpeed = 0.5; turnspeed = 0.8; }
            if (gamepad1.left_trigger > 0) { DriveSpeed = 0.2; turnspeed = 0.9; }
            

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
if (gamepad2.dpad_up)    { ShootAngle.setPosition(0.63); ShooterPower = 0.85; }
if (gamepad2.dpad_right) { ShootAngle.setPosition(0.67); ShooterPower = 0.4; }
if (gamepad2.dpad_down)  { ShootAngle.setPosition(0.7);  ShooterPower = 0.6; }
if (gamepad2.dpad_left)  { ShootAngle.setPosition(0.8);  ShooterPower = 0.7; }

RShooter.setPower(ShooterPower);
LShooter.setPower(ShooterPower);
            //=========================================================
            //                  **CONVEYOR SYSTEM**
            //=========================================================

            // Toggle with left bumper
            if (gamepad2.left_bumper && !prevLeftBumper) {
                conveyorTogglePower = (conveyorTogglePower == 0) ? 1 : 0;
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
            }
            prevBack = gamepad2.back;

            if (cblockClosed) {
                CBLOCK.setPosition(1);
            } else {
                CBLOCK.setPosition(0);
            }
            
            //---rgb lights ---
            boolean shift = true;
            if (shift) {
                double wave = (Math.sin(getRuntime() * 3.0) + 1.0) / 2.0;
                led.setPosition(0.3 + (wave * 0.4)); 
            } else {
                led.setPosition(1.0); // White when Idle
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
            telemetry.update();
        } 

    }
}
