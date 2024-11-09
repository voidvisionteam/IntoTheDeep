package org.firstinspires.ftc.teamcode;
import androidx.appcompat.widget.ActionBarOverlayLayout;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.LED;


@TeleOp(name="Lights")

public class Lights extends OpMode {



        @Override
        public void init() {

                RevBlinkinLedDriver blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
                int[] speakerSeconds = {1, 2, 3};
                ///how long each speaker gets to speak
                RevBlinkinLedDriver.BlinkinPattern[] Colors = {RevBlinkinLedDriver.BlinkinPattern.VIOLET, RevBlinkinLedDriver.BlinkinPattern.BLUE, RevBlinkinLedDriver.BlinkinPattern.GREEN};
                //what color each speaker gets
                int speakerNum = 0;
                int speakerNumLength = speakerSeconds.length;
                double startTime = getRuntime();
                while (speakerNum  < speakerNumLength) {
                        while ((getRuntime() - startTime) < speakerSeconds[speakerNum]) {
                        }
                        blinkinLedDriver.setPattern(Colors[speakerNum]);
                        speakerNum++;
                }
                ;
        }

        @Override
        public void loop() {
                RevBlinkinLedDriver blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
                blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.COLOR_WAVES_OCEAN_PALETTE);
        }
}