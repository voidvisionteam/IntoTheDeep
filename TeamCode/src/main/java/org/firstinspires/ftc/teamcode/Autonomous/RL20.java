package org.firstinspires.ftc.teamcode.Autonomous;

import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.gampad;
import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.packet;
import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.time;
import static java.lang.Math.min;
import static java.lang.Math.toRadians;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robots.BradBot;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequence;

public class RL20 {
  boolean logi = false, isRight = false;
  LinearOpMode op;
  BradBot robot;
  int bark = 0;
  int delaySec = 0;
  TrajectorySequence[] spikey = new TrajectorySequence[3];
  TrajectorySequence[] pathy = new TrajectorySequence[3];
  TrajectorySequence[] droppy = new TrajectorySequence[3];
  TrajectorySequence[] parky = new TrajectorySequence[3];
  TrajectorySequence[] parkyLeft = new TrajectorySequence[3];

  public RL20(LinearOpMode op, boolean isLogi) {
    logi = isLogi;
    this.op = op;
    robot = new BradBot(op, false, isLogi);
    Pose2d startPose = new Pose2d(-36, -60, toRadians(-90));
    robot.roadrun.setPoseEstimate(startPose);

    spikey[0] =
        robot
            .roadrun
            .trajectorySequenceBuilder(startPose)
            .setReversed(true)
            .lineToLinearHeading(new Pose2d(-47, -38, toRadians(-90)))
            .lineToLinearHeading(new Pose2d(-44, -42, toRadians(-90)))
            .build();

    spikey[1] =
        robot
            .roadrun
            .trajectorySequenceBuilder(startPose)
            .setReversed(true)
            .lineToLinearHeading(new Pose2d(-34, -36, toRadians(-90)))
            .build();

    spikey[2] =
        robot
            .roadrun
            .trajectorySequenceBuilder(startPose)
            .setReversed(true)
            .lineToLinearHeading(new Pose2d(-38, -38, toRadians(-90)))
            .lineToLinearHeading(new Pose2d(-30, -38, toRadians(-180)))
            .lineToLinearHeading(new Pose2d(-33.5, -33, toRadians(-180)))
            .build();

    pathy[0] =
        robot
            .roadrun
            .trajectorySequenceBuilder(spikey[0].end())
            .setReversed(false)
            .lineToLinearHeading(new Pose2d(-38, -58.5, toRadians(-180)))
            .setReversed(true)
            .lineToLinearHeading(new Pose2d(42, -58.5, toRadians(-180)))
            .build();

    pathy[1] =
        robot
            .roadrun
            .trajectorySequenceBuilder(spikey[1].end())
            .setReversed(false)
            .lineToLinearHeading(new Pose2d(-38, -58.5, toRadians(-180)))
            .setReversed(true)
            .lineToLinearHeading(new Pose2d(42, -58.5, toRadians(-180)))
            .build();

    pathy[2] =
        robot
            .roadrun
            .trajectorySequenceBuilder(spikey[1].end())
            .setReversed(false)
            .lineToLinearHeading(new Pose2d(-38, -58.5, toRadians(-180)))
            .setReversed(true)
            .lineToLinearHeading(new Pose2d(42, -58.5, toRadians(-180)))
            .build();
    if (!isLogi) {
      droppy[0] =
          robot
              .roadrun
              .trajectorySequenceBuilder(pathy[0].end())
              .lineToLinearHeading(new Pose2d(42.5, -32, toRadians(-180)))
              .lineToLinearHeading(new Pose2d(46.3, -29, toRadians(-180)))
              .build();

      droppy[1] =
          robot
              .roadrun
              .trajectorySequenceBuilder(pathy[1].end())
              .lineToLinearHeading(new Pose2d(42.5, -35, toRadians(-180)))
              .lineToLinearHeading(new Pose2d(46.3, -35.25, toRadians(-180)))
              .build();

      droppy[2] =
          robot
              .roadrun
              .trajectorySequenceBuilder(pathy[2].end())
              .lineToLinearHeading(new Pose2d(42.5, -35, toRadians(-180)))
              .lineToLinearHeading(new Pose2d(46.6, -41.5, toRadians(-180)))
              .build();
    } else {
      droppy[0] =
          robot
              .roadrun
              .trajectorySequenceBuilder(pathy[0].end())
              .lineToLinearHeading(new Pose2d(42.5, -32, toRadians(-180)))
              .waitSeconds(2)
              .lineToLinearHeading(new Pose2d(47.6, -29, toRadians(-180)))
              .build();

      droppy[1] =
          robot
              .roadrun
              .trajectorySequenceBuilder(pathy[1].end())
              .lineToLinearHeading(new Pose2d(42.5, -35, toRadians(-180)))
              .waitSeconds(2)
              .lineToLinearHeading(new Pose2d(47.5, -36, toRadians(-180)))
              .build();

      droppy[2] =
          robot
              .roadrun
              .trajectorySequenceBuilder(pathy[2].end())
              .lineToLinearHeading(new Pose2d(42.5, -35, toRadians(-180)))
              .waitSeconds(2)
              .lineToLinearHeading(new Pose2d(46.6, -42, toRadians(-180)))
              .build();
    }

    parky[0] =
        robot
            .roadrun
            .trajectorySequenceBuilder(droppy[0].end())
            .lineToLinearHeading(new Pose2d(43.3, -29, toRadians(-180)))
            .lineToLinearHeading(new Pose2d(50, -60, toRadians(-180)))
            .build();

    parky[1] =
        robot
            .roadrun
            .trajectorySequenceBuilder(droppy[1].end())
            .lineToLinearHeading(new Pose2d(43.3, -35.5, toRadians(-180)))
            .lineToLinearHeading(new Pose2d(50, -60, toRadians(-180)))
            .build();

    parky[2] =
        robot
            .roadrun
            .trajectorySequenceBuilder(droppy[2].end())
            .lineToLinearHeading(new Pose2d(43.3, -41.5, toRadians(-180)))
            .lineToLinearHeading(new Pose2d(50, -60, toRadians(-180)))
            .build();
    parkyLeft[0] =
            robot
                    .roadrun
                    .trajectorySequenceBuilder(droppy[0].end())
                    .lineToLinearHeading(new Pose2d(43.3, -29, toRadians(-180)))
                    .lineToLinearHeading(new Pose2d(10, -60, toRadians(-180)))
                    .build();

    parkyLeft[1] =
            robot
                    .roadrun
                    .trajectorySequenceBuilder(droppy[1].end())
                    .lineToLinearHeading(new Pose2d(43.3, -35.5, toRadians(-180)))
                    .lineToLinearHeading(new Pose2d(10, -60, toRadians(-180)))
                    .build();

    parkyLeft[2] =
            robot
                    .roadrun
                    .trajectorySequenceBuilder(droppy[2].end())
                    .lineToLinearHeading(new Pose2d(43.3, -41.5, toRadians(-180)))
                    .lineToLinearHeading(new Pose2d(10, -60, toRadians(-180)))
                    .build();
    robot.dropServo(1);
    robot.setRight(false);
    robot.setBlue(false);
    robot.observeSpike();
  }

  public void waitForStart() {
    while (!op.isStarted() || op.isStopRequested()) {
      bark = robot.getSpikePos();
      op.telemetry.addData("pixel", bark);
      packet.put("spike", bark);
      op.telemetry.addData("delaySec", delaySec);
      op.telemetry.addData("isRight", isRight);
      if (gampad.readGamepad(op.gamepad1.dpad_up, "gamepad1_dpad_up", "addSecs")) {
        delaySec++;
      }
      if (gampad.readGamepad(op.gamepad1.dpad_down, "gamepad1_dpad_down", "minusSecs")) {
        delaySec = min(0, delaySec - 1);
      }
      if (gampad.readGamepad(op.gamepad1.dpad_right, "gamepad1_dpad_right", "parkRight")) {
        isRight = true;
      }
      if (gampad.readGamepad(op.gamepad1.dpad_left, "gamepad1_dpad_left", "parkLeft")) {
        isRight = false;
      }
      robot.update();
    }
    op.resetRuntime();
    time=0;
  }

  public void purp() {
    robot.queuer.queue(false, true);
    robot.queuer.addDelay(2.0 + delaySec);
    robot.queuer.queue(false, true);
    robot.upAuto();
    robot.purpurAuto();
    robot.queuer.addDelay(1.0);
    robot.followTrajSeq(spikey[bark]);
    robot.queuer.addDelay(0.2);
    robot.dropAuto(0);
  }

  public void pre() {
    robot.queuer.addDelay(0.3);
    robot.followTrajSeq(pathy[bark]);
    robot.queuer.addDelay(.6);
    robot.resetAuto();
    robot.grabSupAuto();
    robot.followTrajSeq(droppy[bark]);
    robot.queuer.addDelay(.5);
    robot.lowAuto();
    robot.queuer.addDelay(1.5);
    robot.veryLowAuto();
    robot.drop();
  }

  public void park() {
    robot.queuer.addDelay(.5);
    robot.resetAuto();
    if(isRight){
      robot.followTrajSeq(parky[bark]);
    } else{
      robot.followTrajSeq(parkyLeft[bark]);
    }
    robot.queuer.waitForFinish();
    robot.queuer.queue(false, true);
  }

  public void update() {
    robot.update();
    robot.update();
    robot.queuer.setFirstLoop(false);
  }

  public boolean isAutDone() {
    return !robot.queuer.isFullfilled() && time < 29.8;
  }
}
