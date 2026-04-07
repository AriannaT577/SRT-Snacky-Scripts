import cv2
import serial
import time

arduino = serial.Serial(port='COM8', baudrate=9600, timeout=1)
time.sleep(2)

cap = cv2.VideoCapture(0) #webcam

# Define color range (example: red in HSV)
lowerc = (0, 120, 70)
upperc = (10, 255, 255)

while True:
    ret, frame = cap.read()
    if not ret:
        break

    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV) #hsv to colour

    mask = cv2.inRange(hsv, lowerc, upperc)
    c_detected = cv2.countNonZero(mask) > 5000  #how racist/threshold

    cv2.imshow("Camera", frame) #shows camera
    cv2.imshow("Mask", mask)

    if c_detected:
        arduino.write(b'1')  #racism deteched, spray!!
        print("deteched: spraying")
    else:
        arduino.write(b'0')  #stop spraying
        print("idle")

    # Exit on 'q'
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
arduino.close()
