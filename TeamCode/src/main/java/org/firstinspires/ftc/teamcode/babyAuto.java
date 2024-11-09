package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


@Autonomous(name="babyAuto")
public class babyAuto extends OpMode {

    @Override
    public void init() {
        //Definitions
        babyhwmap robot=new babyhwmap();
        //Definitions
        //drive to basket

        //place block

        //drive to block 1
        //pick up block
        //drive to basket
        //put it in

        //drive to block 2
        //pick up block
        //drive to basket
        //put it in

        //drive to block 3
        //pick up block
        //drive to basket
        //put it in

        //park
    }
        @Override
        public void loop(){
            //Leave blank intentionally :)
        }
}

/*configuration:
    0 leftBack
    1 leftFront
    2 rightFront
    3 rightBack

    0 servo hippo1
    1 servo hippo 2
    3 servo basket1
    4 servo basket2
    5 continuous rotation servo intake

    0 Digital Device leftEncoderA
    1 Digital Device
    4 Digital Device
    5 Digital Device

 */

