//IMPORT
import java.util.Random;

//VARIBALES OR SOMETHING
    private Servo led;

//PREVENTS THE TWITCHING?
    private boolean prevLeftBumper = false;
    private boolean prevRightBumper = false;
    private boolean prevBack = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadRight = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;

 //COLOUR LOGIC
    private String[] colors = {"Red","Orange","Yellow","Sage","Green","Azure","Blue","Violet","White"};//list of colours
    private double[] colorPositions = {0.277, 0.333, 0.388, 0.444, 0.5, 0.555, 0.611, 0.722, 1.0}; //w their repseotice colour on the servo thingy
    private Random random = new Random();
    private int actionValue = 0; 
    private int lastColorIndex = 0;  

    private void pickRandomColor() { //same as a def function in python
        actionValue++;
        lastColorIndex = random.nextInt(colors.length);
        led.setPosition(colorPositions[lastColorIndex]);
    }

//HARDWARE
        led = hardwareMap.get(Servo.class, "led");

//WHILE OP MODE IS ACTIVE
//in any button OR control on thr controller with a PREV (to prevent the twitching) add this in the if statement
pickRandomColor();

