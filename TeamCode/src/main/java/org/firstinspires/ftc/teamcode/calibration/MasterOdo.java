package org.firstinspires.ftc.teamcode.calibration;

import android.graphics.Point;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import org.firstinspires.ftc.teamcode.autonomous.AutoDot;
import org.firstinspires.ftc.teamcode.autonomous.AutoRoute;
import org.firstinspires.ftc.teamcode.autonomous.AutoStep;
import org.firstinspires.ftc.teamcode.bots.BotAction;
import org.firstinspires.ftc.teamcode.bots.BotActionObj;
import org.firstinspires.ftc.teamcode.bots.BotMoveProfile;
import org.firstinspires.ftc.teamcode.bots.MoveStrategy;
import org.firstinspires.ftc.teamcode.bots.RobotDirection;
import org.firstinspires.ftc.teamcode.bots.UltimateBot;
import org.firstinspires.ftc.teamcode.odometry.OdoBase;
import org.firstinspires.ftc.teamcode.odometry.RobotCoordinatePosition;
import org.firstinspires.ftc.teamcode.skills.Led;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.firstinspires.ftc.teamcode.autonomous.AutoRoute.NAME_BLUE;
import static org.firstinspires.ftc.teamcode.autonomous.AutoRoute.NAME_RED;
import static org.firstinspires.ftc.teamcode.autonomous.AutoStep.NO_ACTION;

@TeleOp(name="Master Odo", group="Robot15173")
@Disabled
public class MasterOdo extends OdoBase {

    public static String COORDINATE = "Coordinate";

    protected ArrayList<AutoRoute> routes = new ArrayList<>();
    protected ArrayList<Integer> blueRoutes = new ArrayList<>();
    protected ArrayList<Integer> redRoutes = new ArrayList<>();

    protected double right = 0;
    protected double left = 0;


    protected int selectedTopMode = 0;
    protected int selectedGoToMode = 0;
    protected boolean topMode = true;
    protected boolean goToMode = false;

    protected boolean routeListMode = false;

    protected boolean startSettingMode  = false;
    protected boolean routeSettingMode  = false;
    protected boolean XSettingMode = true;
    protected boolean YSettingMode = false;
    protected boolean initHeadSettingsMode = false;
    protected boolean speedSettingMode = false;
    protected boolean strategySettingMode = false;
    protected boolean waitSettingMode = false;
    protected boolean desiredHeadSettingMode = false;
    protected boolean routeSavingMode = false;
    protected boolean actionSettingMode = false;
    protected boolean directionSettingMode = false;

    protected boolean dynamicDestinationMode = false;

    protected boolean stopSettingMode = false;

    protected boolean routeNameSettingMode = true;
    protected boolean routeIndexSettingMode = false;

    protected boolean manualDriveMode = false;
    protected boolean coordinateSavingMode = false;

    protected boolean locatorStartMode = false;

    protected AutoDot newDot = new AutoDot();

    protected AutoStep goToInstructions = new AutoStep();
    protected AutoRoute newRoute = new AutoRoute();



    protected static final int[] modesTop = new int[]{0, 1, 2, 3, 4, 5};
    protected static final String[] modeNamesTop = new String[]{"Start Position", "Go To", "Routes", "Save Route", "Manual Drive", "Start Locator"};

    protected static final int[] modesStep = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    protected static final String[] modeStepName = new String[]{"Destination", "Top Speed", "Strategy", "Wait", "Continue", "Heading", "Action", "Direction"};


    protected double SPEED_INCREMENT = 0.1;

    protected static int HEAD_INCREMENT = 45;
    protected int headIncrementValue = 1;

    protected Led led = null;

    protected Deadline gamepadRateLimit;
    protected final static int GAMEPAD_LOCKOUT = 500;

    private static final String TAG = "MasterOdo";



    @Override
    public void runOpMode() throws InterruptedException {
        try {
            super.runOpMode();
//            this.led = bot.getLights();
            gamepadRateLimit = new Deadline(GAMEPAD_LOCKOUT, TimeUnit.MILLISECONDS);
            listRoutes();

            waitForStart();

            if (this.led != null){
                this.led.none();
            }

            showConfig();

            while (opModeIsActive()) {
                processCommands();
            }
        }
        catch (Exception ex){
            telemetry.addData("Error", ex.getMessage());
            telemetry.update();
            sleep(15000);
        }
        finally {
            if (locator != null){
                locator.stop();
            }
            selectedRoute.getSteps().clear();
            selectedRoute = null;
        }
    }

    @Override
    protected void initBot() {
        this.bot = new UltimateBot();
    }

    @Override
    protected void initLocator() {
        if (locator == null) {
            this.locator = new RobotCoordinatePosition(bot, new Point(startX, startY), initHead, RobotCoordinatePosition.THREAD_INTERVAL);
            locator.reverseHorEncoder();
            locator.setPersistPosition(true);
            startLocator(locator);
        }
    }

    private void toggleRouteSettings(){
        if (startSettingMode) {
            if (XSettingMode) {
                XSettingMode = false;
                YSettingMode = true;
                initHeadSettingsMode = false;
            } else if (YSettingMode) {
                YSettingMode = false;
                XSettingMode = false;
                initHeadSettingsMode = true;
            } else if (initHeadSettingsMode) {
                initHeadSettingsMode = false;
                YSettingMode = false;
                XSettingMode = true;
            }
        }
        else{
            if (XSettingMode) {
                XSettingMode = false;
                YSettingMode = true;
            } else if (YSettingMode) {
                YSettingMode = false;
                XSettingMode = true;
            }
            else if (!XSettingMode && !YSettingMode){
                XSettingMode = true;
            }
        }
    }

    private void toggleRouteNaming(){
        if (routeNameSettingMode){
            routeNameSettingMode = false;
            routeIndexSettingMode = true;
        }
        else if (routeIndexSettingMode){
            routeIndexSettingMode = false;
            routeNameSettingMode = true;
        }
    }

    private void processDriveCommands(){
        double drive = gamepad1.left_stick_y;
        double turn = 0;
        double ltrigger = gamepad1.left_trigger;
        double rtrigger = gamepad1.right_trigger;
        if (ltrigger > 0){
            turn = -ltrigger;
        }
        else if (rtrigger > 0){
            turn = rtrigger;
        }

        double strafe = gamepad1.right_stick_x;


        if (Math.abs(strafe) > 0) {
            if (strafe < 0) {
                bot.strafeRight(Math.abs(strafe));
            } else {
                bot.strafeLeft(Math.abs(strafe));
            }
        } else {
            bot.move(drive, turn);
        }
    }


    private void processCommands(){
        if (!gamepadRateLimit.hasExpired()) {
            return;
        }

        if (gamepad1.b && routeListMode){
            //clone selected Route
            cloneSelectedRoute();
            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.a && (XSettingMode || YSettingMode)){
            dynamicDestinationMode = !dynamicDestinationMode;

            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.back){

            topMode = true;
            goToMode = false;

            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.left_bumper){
            if (desiredHeadSettingMode || startSettingMode){
                switch (headIncrementValue){
                    case 1:
                        headIncrementValue = 10;
                        break;
                    case 10:
                        headIncrementValue = HEAD_INCREMENT;
                        break;
                    case 45:
                        headIncrementValue = 1;
                        break;
                }
            }
            showConfig();
            gamepadRateLimit.reset();
        }



        if (gamepad1.dpad_left ){

            if (routeSettingMode || startSettingMode){
                toggleRouteSettings();
            }
            else if (routeSavingMode){
                toggleRouteNaming();
            }

//            MODE_VALUE = -changeIncrement();
            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.dpad_right){
            if (routeSettingMode || startSettingMode){
                toggleRouteSettings();
            }
            else if (routeSavingMode){
                toggleRouteNaming();
            }

//            MODE_VALUE = changeIncrement();
            showConfig();
            gamepadRateLimit.reset();
        }

        //value adjustment
        if (gamepad1.dpad_down){
            if (routeSettingMode){
                if (dynamicDestinationMode){
                    String targetRef = goToInstructions.getTargetReference();
                    Object [] keys = coordinateFunctions.keySet().toArray();
                    int index = -1;
                    boolean found = false;
                    for (int i = 0; i < keys.length; i++){
                        if (targetRef.equals(keys[i])){
                            index = i+1;
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        index++;
                    }

                    if (index < 0 || index >= keys.length){
                        goToInstructions.setTargetReference("");
                    }
                    else{
                        goToInstructions.setTargetReference(keys[index].toString());
                    }

                }
                else if (XSettingMode) {
                    int x = goToInstructions.getTargetX();
                    x -= 5;
                    goToInstructions.setTargetX(x);
                }
                else if (YSettingMode){
                    int y = goToInstructions.getTargetY();
                    y -= 5;
                    goToInstructions.setTargetY(y);
                }

            }
            else if (startSettingMode){
                if (XSettingMode) {
                    startX -= 5;
                }
                else if (YSettingMode){
                    startY -= 5;
                }
                else if (initHeadSettingsMode){
                    if (initHead == -1){
                        initHead = 0;
                    }
                    initHead -= headIncrementValue;
                    if (initHead < 0){
                        initHead = 0;
                    }
                }
                goToInstructions.setTargetX(startX);
                goToInstructions.setTargetY(startY);
            }
            else if (speedSettingMode){
                double speed = goToInstructions.getTopSpeed();
                speed = speed - SPEED_INCREMENT;
                if (speed < 0){
                    speed = 0;
                }
                goToInstructions.setTopSpeed(speed);
            }
            else if (routeListMode){
                for(int i = 0; i < routes.size(); i++){
                    AutoRoute r = routes.get(i);
                    if (r.isSelected()){
                        r.setSelected(false);
                        AutoRoute selected = null;
                        if (i + 1 >= routes.size()){
                            selected = routes.get(0);
                            selected.setSelected(true);
                        }
                        else{
                            selected = routes.get(i+1);
                            selected.setSelected(true);
                        }
                        selectedRoute = selected;
                        break;
                    }
                }
            }
            else if (strategySettingMode){
                int index = goToInstructions.getMoveStrategy().ordinal();
                int total = MoveStrategy.values().length;
                index++;
                if (index >= total){
                    index = 0;
                }
                MoveStrategy updated = MoveStrategy.values()[index];
                goToInstructions.setMoveStrategy(updated);

            }
            else if (directionSettingMode){
                int index = goToInstructions.getRobotDirection().ordinal();
                int total = RobotDirection.values().length;
                index++;
                if (index >= total){
                    index = 0;
                }
                RobotDirection updated = RobotDirection.values()[index];
                goToInstructions.setRobotDirection(updated);
            }
            else if (desiredHeadSettingMode){
                double desiredHead = goToInstructions.getDesiredHead();
                if (desiredHead < -1){
                    desiredHead = -1;
                }
                desiredHead -= headIncrementValue;
                if (desiredHead < -1){
                    desiredHead = -1;
                }
                goToInstructions.setDesiredHead(desiredHead);
            }
            else if (waitSettingMode){
                int waitMS = goToInstructions.getWaitMS();
                waitMS -= 500;
                if (waitMS < 0){
                    waitMS = 0;
                }
                goToInstructions.setWaitMS(waitMS);
            }
            else if(stopSettingMode){
                boolean continous = goToInstructions.isContinuous();
                goToInstructions.setContinuous(!continous);
            }
            else if (actionSettingMode){
                String action = goToInstructions.getAction();
                int selectedIndex = -1;
                for(int i = 0; i < botActions.size(); i++){
                    if (botActions.get(i).getName().equals(action)){
                        selectedIndex = i;
                        break;
                    }
                }
                selectedIndex++;
                if (selectedIndex == botActions.size()){
                    selectedIndex = -1;
                }
                if (selectedIndex == -1){
                    goToInstructions.setAction(NO_ACTION);
                }
                else{
                    goToInstructions.setAction(botActions.get(selectedIndex).getName());
                }
            }
            else if (coordinateSavingMode){
                String dotName = newDot.getDotName();
                int ascii = (int) dotName.charAt(0);
                ascii--;
                if (ascii < AutoDot.asciiA){
                    ascii = AutoDot.asciiZ;
                }
                newDot.setDotName(Character.toString ((char) ascii));
            }
            else if (routeSavingMode) {
                if (routeIndexSettingMode){
                    int i = selectedRoute.getNameIndex();
                    ArrayList<Integer> list = blueRoutes;
                    if (selectedRoute.getName() == NAME_RED){
                        list = redRoutes;
                    }
                    i--;
                    while(list.contains(i)) {
                        i--;
                    }
                    if (i > 0) {
                        selectedRoute.setNameIndex(i);
                    }
                }
                else if (routeNameSettingMode){
                    String name = selectedRoute.getName();
                    if (name.equals(NAME_BLUE)){
                        if (selectedRoute.getNameIndex() == 0){
                            selectedRoute.setNameIndex(getMinAvailableIndex(redRoutes));
                        }
                        name = NAME_RED;
                    }
                    else{
                        if (selectedRoute.getNameIndex() == 0){
                            selectedRoute.setNameIndex(getMinAvailableIndex(blueRoutes));
                        }
                    }
                selectedRoute.setName(name);
                }
            }
            else{
                if (topMode) {
                    if (selectedTopMode < modesTop.length) {
                        selectedTopMode++;
                        if (selectedTopMode == modesTop.length-1){
                            locatorStartMode = true;
                        }
                        else{
                            locatorStartMode = false;
                        }
                    }
                }
                else if (goToMode){
                    if (selectedGoToMode < modesStep.length) {
                        selectedGoToMode++;
                    }
                }
            }
            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.dpad_up){
            if (routeSettingMode){
                if (dynamicDestinationMode){
                    String targetRef = goToInstructions.getTargetReference();
                    Object [] keys = coordinateFunctions.keySet().toArray();
                    int index = -1;
                    boolean found = false;
                    for (int i = 0; i < keys.length; i++){
                        if (targetRef.equals(keys[i])){
                            index = i-1;
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        index = keys.length -1;
                    }
                    if (index < 0 || index >= keys.length){
                        goToInstructions.setTargetReference("");
                    }
                    else{
                        goToInstructions.setTargetReference(keys[index].toString());
                    }

                }
                else if (XSettingMode) {
                    int x = goToInstructions.getTargetX();
                    x += 5;
                    goToInstructions.setTargetX(x);
                }
                else if (YSettingMode){
                    int y = goToInstructions.getTargetY();
                    y += 5;
                    goToInstructions.setTargetY(y);
                }

            }
            else if (startSettingMode){
                if (XSettingMode) {
                    startX += 5;
                }
                else if (YSettingMode){
                    startY += 5;
                }
                else if (initHeadSettingsMode){
                    if (initHead == -1){
                        initHead = 0;
                    }
                    initHead += headIncrementValue;
                }
                goToInstructions.setTargetX(startX);
                goToInstructions.setTargetY(startY);
            }
            else if (speedSettingMode){
                double speed = goToInstructions.getTopSpeed();
                speed = speed + SPEED_INCREMENT;
                if (speed > 1){
                    speed = 1;
                }
                goToInstructions.setTopSpeed(speed);
            }
            else if (routeListMode){
                for(int i = routes.size() - 1; i >= 0; i--){
                    AutoRoute r = routes.get(i);
                    if (r.isSelected()){
                        r.setSelected(false);
                        AutoRoute selected = null;
                        if (i <= 0){
                            selected = routes.get(routes.size() -1 );
                            selected.setSelected(true);
                        }
                        else{
                            selected = routes.get(i -i);
                            selected.setSelected(true);
                        }
                        selectedRoute = selected;
                        break;
                    }
                }
            }
            else if (desiredHeadSettingMode){
                double desiredHead = goToInstructions.getDesiredHead();

                desiredHead += headIncrementValue;
                goToInstructions.setDesiredHead(desiredHead);
            }
            else if (waitSettingMode){
                int waitMS = goToInstructions.getWaitMS();
                waitMS += 500;
                goToInstructions.setWaitMS(waitMS);
            }
            else if(stopSettingMode){
                boolean continuous = goToInstructions.isContinuous();
                goToInstructions.setContinuous(!continuous);
            }
            else if (strategySettingMode){
                int index = goToInstructions.getMoveStrategy().ordinal();
                int total = MoveStrategy.values().length;
                index--;
                if (index < 0){
                    index = total - 1;
                }
                MoveStrategy updated = MoveStrategy.values()[index];
                goToInstructions.setMoveStrategy(updated);

            }
            else if (directionSettingMode){
                int index = goToInstructions.getRobotDirection().ordinal();
                int total = RobotDirection.values().length;
                index--;
                if (index < 0){
                    index = total - 1;
                }
                RobotDirection updated = RobotDirection.values()[index];
                goToInstructions.setRobotDirection(updated);
            }
            else if (actionSettingMode){
                String action = goToInstructions.getAction();
                int selectedIndex = -1;
                for(int i = 0; i < botActions.size(); i++){
                    if (botActions.get(i).getName().equals(action)){
                        selectedIndex = i;
                        break;
                    }
                }
                selectedIndex--;
                if (selectedIndex < -1){
                    selectedIndex = botActions.size() - 1;
                }
                if (selectedIndex == -1){
                    goToInstructions.setAction(NO_ACTION);
                }
                else{
                    goToInstructions.setAction(botActions.get(selectedIndex).getName());
                }
            }
            else if (coordinateSavingMode){
                String dotName = newDot.getDotName();
                int ascii = (int) dotName.charAt(0);
                ascii++;
                if (ascii > AutoDot.asciiZ){
                    ascii = AutoDot.asciiA;
                }
                newDot.setDotName(Character.toString ((char) ascii));
            }
            else if (routeSavingMode) {
                if (routeIndexSettingMode){
                    int i = selectedRoute.getNameIndex();
                    ArrayList<Integer> list = blueRoutes;
                    if (selectedRoute.getName() == NAME_RED){
                        list = redRoutes;
                    }
                    i++;
                    while(list.contains(i)) {
                        i++;
                    }
                    selectedRoute.setNameIndex(i);
                }
                else if (routeNameSettingMode){
                    String name = selectedRoute.getName();
                    if (name.equals(NAME_RED)){
                        if (selectedRoute.getNameIndex() == 0){
                            selectedRoute.setNameIndex(getMinAvailableIndex(blueRoutes));
                        }
                        name = NAME_BLUE;
                    }
                    else{
                        if (selectedRoute.getNameIndex() == 0){
                            selectedRoute.setNameIndex(getMinAvailableIndex(redRoutes));
                        }
                    }
                    selectedRoute.setName(name);
                    //set minimal index
                }
            }
            else{
                if (topMode) {
                    if (selectedTopMode > 0) {
                        selectedTopMode--;
                        if (selectedTopMode < modesTop.length - 1){
                            locatorStartMode = false;
                        }
                    }
                }
                else if (goToMode){
                    if (selectedGoToMode > 0) {
                        selectedGoToMode--;
                    }
                }
            }

            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.start){
            Log.d(TAG, String.format("Start pressed. Goto mode is %b", goToMode ));
            if (goToMode){
                initLocator(); // in case it was not initialized
                goTo(this.goToInstructions, true, NAME_RED);
            }
            else if (routeSavingMode){
                saveRoute();
            }
            else if (routeListMode){
                runSelectedRoute();
            }
            else if (manualDriveMode){
                if (!coordinateSavingMode){
                    coordinateSavingMode = true;
                }
                else{
                    coordinateSavingMode = false;
                    saveCoordinate();
                }
            }
            else if (locatorStartMode){
                initLocator();
            }
            showConfig();
            gamepadRateLimit.reset();
        }

        if (gamepad1.x){
            if (desiredHeadSettingMode){
                goToInstructions.setDesiredHead(BotMoveProfile.DEFAULT_HEADING);
            }
            if (routeListMode){
                deleteSelectedRoute();
            }
            showConfig();
            gamepadRateLimit.reset();
        }

        //accept
        if (gamepad1.right_bumper){
            if (topMode){
                switch (selectedTopMode){
                    case 0:
                        startSettingMode = !startSettingMode;
                        break;
                    case 1:
                        topMode = false;
                        goToMode = true;
                        break;
                    case 2:
                        routeListMode = !routeListMode;
                        break;
                    case 3:
                        routeSavingMode = !routeSavingMode;
                        break;
                    case 4:
                        manualDriveMode = !manualDriveMode;
                        break;
                }
            }
            else if (goToMode){
                switch (selectedGoToMode){
                    case 0:
                        routeSettingMode = !routeSettingMode;
                        break;
                    case 1:
                        speedSettingMode = !speedSettingMode;
                        break;
                    case 2:
                        strategySettingMode = !strategySettingMode;
                        break;
                    case 3:
                        waitSettingMode = !waitSettingMode;
                        break;
                    case 4:
                        stopSettingMode = !stopSettingMode;
                        break;
                    case 5:
                        desiredHeadSettingMode = !desiredHeadSettingMode;
                        break;
                    case 6:
                        actionSettingMode = !actionSettingMode;
                        break;
                    case 7:
                        directionSettingMode = !directionSettingMode;
                        break;
                }
            }
            showConfig();
            gamepadRateLimit.reset();
        }

        // manual drive
        if (manualDriveMode){
            processDriveCommands();
            showConfig();
        }
    }


    private String getStepValue(int index){
        String val = "";
        switch (index){
            case 0:
                //destination
                val = goToInstructions.getDestination();
                break;
            case 1:
                val = goToInstructions.getTopSpeedString();
                break;
            case 2:
                val = goToInstructions.getMoveStrategyString();
                break;
            case 3:
                val = goToInstructions.getWaitString();
                break;
            case 4:
                val = goToInstructions.isContinuousAsString();
                break;
            case 5:
                val = goToInstructions.getDesiredHeadString();
                break;
            case 6:
                val = goToInstructions.getAction();
                break;
            case 7:
                val = goToInstructions.getRobotDirectionString();
                break;
        }
        return val;
    }

    private void showConfig(){
        try {
            if (routeSettingMode) {
                showTarget();
            } else if (startSettingMode) {
                showStart();
            } else if (speedSettingMode) {
                telemetry.addData("Top Speed", "%.2f", goToInstructions.getTopSpeed());
            }
            else if (desiredHeadSettingMode){
                telemetry.addData("Heading Increment", headIncrementValue);
                showHeading();
                telemetry.addData("Desired Head", "%.2f", goToInstructions.getDesiredHead());
            }
            else if (routeListMode){
                telemetry.addData("Play", "to run selected route");
                telemetry.addData("B", "to clone");
                telemetry.addData(" ", " ");
                for (AutoRoute r : routes) {
                    String routeVal = r.getRouteName();
                    if (r.getLastRunTime() > 0){
                        routeVal = String.format("%s (%d ms)", routeVal, r.getLastRunTime());
                    }
                    if (r.isSelected()) {
                        telemetry.addData(routeVal, "*");
                    } else {
                        telemetry.addData(routeVal, " ");
                    }
                }
            }
            else if (strategySettingMode) {
                for (MoveStrategy s : MoveStrategy.values()) {
                    if (goToInstructions.getMoveStrategy().equals(s)) {
                        telemetry.addData(s.name(), "*");
                    } else {
                        telemetry.addData(s.name(), " ");
                    }
                }
            }
            else if (directionSettingMode){
                for (RobotDirection rd : RobotDirection.values()) {
                    if (goToInstructions.getRobotDirection().equals(rd)) {
                        telemetry.addData(rd.name(), "*");
                    } else {
                        telemetry.addData(rd.name(), " ");
                    }
                }
            }
            else if (routeSavingMode){
                if (routeIndexSettingMode) {
                    telemetry.addData(selectedRoute.getName(), String.format("-%d*", selectedRoute.getNameIndex()));
                }
                else if(routeNameSettingMode){
                    telemetry.addData(String.format("%s*", selectedRoute.getName()), String.format("-%d", selectedRoute.getNameIndex()));
                }
            }
            else if (manualDriveMode){
                if (coordinateSavingMode){
                    telemetry.addData("New Named Coordinate", newDot.getDotName() );
                }else {
                    telemetry.addData("Manual Drive Mode", "Use sticks to operate the robot");
                    telemetry.addData("Save coordinates", "Press Start");
                    if (locator != null) {
                        telemetry.addData("X ", locator.getCurrentX());
                        telemetry.addData("Y ", locator.getCurrentY());
                        telemetry.addData("Orientation (Degrees)", locator.getOrientation());
                    }
                }
            }
            else if (waitSettingMode) {
                telemetry.addData("Initial Wait Time MS", goToInstructions.getWaitString());
            }
            else if (actionSettingMode){
                String selected = " ";
                if (goToInstructions.getAction().equals(NO_ACTION)){
                    selected = "*";
                }
                telemetry.addData("None", selected);
                for(Method m : this.botActions){
                    selected = "";
                    String name = m.getName();
                    String displayName = name;
                    BotAction annotation = m.getAnnotation(BotAction.class);
                    if (annotation != null){
                        displayName = annotation.displayName();
                    }
                    if(coordinateFunctions.containsKey(name)){
                        AutoDot val = coordinateFunctions.get(name);
                        if (val != null){
                            selected = val.toString();
                        }
                    }
                    if (goToInstructions.getAction().equals(name)){
                        selected = String.format("%s *", selected);
                    }
                    telemetry.addData(displayName, selected);
                }
            }
            else if (stopSettingMode){
                telemetry.addData("Continue", goToInstructions.isContinuous());
            }
            else if (topMode) {
                for (int i = 0; i < modesTop.length; i++) {
                    String selected = i == selectedTopMode ? "*" : " ";
                    if (i == modesTop.length - 1 && locator != null){
                        telemetry.addData(selected, "Locator is running");
                    }
                    else{
                        telemetry.addData(selected, modeNamesTop[i]);
                    }
                }
            } else if (goToMode) {
                showHeading();
                showStart();
                for (int i = 0; i < modesStep.length; i++) {
                    String selected = i == selectedGoToMode ? "*" : " ";
                    telemetry.addData(String.format("%s%s", selected, modeStepName[i]), getStepValue(i));
                }
            }

            if(locator != null) {
                telemetry.addData("\n\nLocator X", this.locator.getCurrentX());
                telemetry.addData("Locator Y", this.locator.getCurrentY());
                telemetry.addData("Locator heading", this.locator.getAdjustedCurrentHeading());
            }
            telemetry.update();
        }
        catch (Exception ex){
            telemetry.addData("Error", ex.getMessage());
            telemetry.update();
        }
    }

    private void showTarget(){
        String toX = XSettingMode ? "*" : " ";
        String toY = YSettingMode ? "*" : " ";

        telemetry.addData("Target", "%d%s : %d%s", goToInstructions.getTargetX(), toX, goToInstructions.getTargetY(), toY);
        telemetry.addData("Dynamic values", "Press A");
        if (dynamicDestinationMode) {
            String targetRef = goToInstructions.getTargetReference();
            String selected = targetRef.equals("") ? "*" : " ";
            telemetry.addData("None ", selected);
            for (Map.Entry<String, AutoDot> entry : coordinateFunctions.entrySet()) {
                selected = targetRef.equals(entry.getKey()) ? "*" : " ";
                if (entry.getValue() != null){
                    selected = String.format("%s %s", entry.getValue().toString(), selected);
                }
                telemetry.addData("Result of ", String.format("%s %s", entry.getKey(), selected));
            }
        }
    }

    private void showStart(){
        String toX = XSettingMode ? "*" : " ";
        String toY = YSettingMode ? "*" : " ";
        String head = initHeadSettingsMode ? "*" : " ";

        telemetry.addData("Increment", headIncrementValue);

        if (locator != null) {
            telemetry.addData("Start", "%d%s : %d%s : %d%s", (int) locator.getCurrentX(), toX, (int) locator.getCurrentY(), toY, (int) locator.getAdjustedCurrentHeading(), head);
        }
        else{
            telemetry.addData("Start", "%d%s : %d%s : %d%s", startX, toX, startY, toY, (int)initHead, head);
        }
    }

    private void showHeading(){
        if (locator != null) {
            telemetry.addData("Current Heading", "%.2f", locator.getAdjustedCurrentHeading());
        }
    }


    private void saveRoute(){
        try{
            if (selectedRoute.getSteps().size() > 0) {
                boolean newRoute = selectedRoute.isTemp();
                selectedRoute.setTemp(false);

                String name = selectedRoute.getRouteName();
                File configFile = getRouteFile(name);

                String jsonPath = selectedRoute.serialize();
                ReadWriteFile.writeFile(configFile, jsonPath);

                if (newRoute){
                    initRoute();
                    addRoute(this.newRoute);
                }
                selectedTopMode = 2;
                routeListMode = true;
                routeSavingMode = false;
                topMode = true;
            }
        }
        catch (Exception e) {
            telemetry.addData("Error", "Route cannot be saved. %s", e.getMessage());
            telemetry.update();
        }
    }

    private void cloneSelectedRoute(){
        try{
            if (selectedRoute.getSteps().size() > 0) {
                boolean newRoute = selectedRoute.isTemp();
                if (newRoute){
                    return;
                }
                AutoRoute cloned = selectedRoute.clone();
                cloned.setSelected(false);
                if (cloned.getName().equals(NAME_BLUE)){
                    cloned.setNameIndex(getMinAvailableIndex(blueRoutes));
                }
                else{
                    cloned.setNameIndex(getMinAvailableIndex(redRoutes));
                }

                String name = cloned.getRouteName();
                File configFile = getRouteFile(name);

                String jsonPath = cloned.serialize();
                ReadWriteFile.writeFile(configFile, jsonPath);

                addRoute(cloned);

                selectedTopMode = 2;
                routeListMode = true;
                routeSavingMode = false;
                topMode = true;
            }
        }
        catch (Exception e) {
            telemetry.addData("Error", "Route cannot be cloned. %s", e.getMessage());
            telemetry.update();
        }
    }

    private void saveRouteFile(AutoRoute route){
        String name = route.getRouteName();
        File configFile = getRouteFile(name);

        String jsonPath = route.serialize();
        ReadWriteFile.writeFile(configFile, jsonPath);
    }

    private void saveCoordinate(){
        try {
            File configFile = getCoordinateFile(newDot.getFileName());

            newDot.setX((int) locator.getCurrentX());
            newDot.setY((int) locator.getCurrentY());
            newDot.setHeading(locator.getOrientation());

            String jsonPath = newDot.serialize();
            ReadWriteFile.writeFile(configFile, jsonPath);

            addNamedCoordinate(newDot);

            newDot = new AutoDot();
        }
        catch (Exception ex){
            telemetry.addData("Error", "Coordinate cannot be saved. %s", ex.getMessage());
            telemetry.update();
        }
    }


    private void runSelectedRoute(){
        try {
            AutoRoute selected = null;
            for (AutoRoute r : routes) {
                if (r.isSelected()) {
                    selected = r;
                    break;
                }
            }
            if (selected != null) {
                locator.init(selected.getStart(), selected.getInitRotation());
                bot.initDetectorThread(selected.getName(), this);
                long startTime = System.currentTimeMillis();
                for (AutoStep s : selected.getSteps()) {
                    this.goTo(s, false, selected.getName());
                }
                long endTime = System.currentTimeMillis();
                selected.setLastRunTime(endTime - startTime);
                saveRouteFile(selected);
            }
        }
        catch (Exception ex){
            telemetry.addData("Error", "Run selected route. %s", ex.getMessage());
        }
        finally {
            bot.stopDetection();
        }
    }

    private void deleteSelectedRoute(){
        String selectedName = "";
        for (int i  = 0; i < routes.size(); i++){
            AutoRoute r = routes.get(i);
            if (r.isTemp()){
                continue;
            }
            if (r.isSelected()){
                selectedName = r.getRouteName();
                try {
                    File f = getRouteFile(selectedName);
                    routes.remove(i);
                    f.delete();

                    int index = r.getNameIndex();
                    if (r.getName().equals(NAME_BLUE) ){
                        clearRouteCache(this.blueRoutes, index);
                    }
                    else if  (r.getName().equals(NAME_RED)){
                        clearRouteCache(this.redRoutes, index);
                    }
                    if (routes.size() > 0){
                        routes.get(0).setSelected(true);
                        selectedRoute = routes.get(0);
                    }

                }
                catch (Exception ex){
                    telemetry.addData("Error", ex.getMessage());
                    telemetry.update();
                }
                break;
            }
        }
    }

    private static void clearRouteCache(ArrayList<Integer> cache, int indexValue){
        for (int i = 0; i < cache.size(); i++){
            if (cache.get(i).equals(indexValue)){
                cache.remove(i);
                break;
            }
        }
    }

    @Override
    protected ArrayList<BotActionObj> loadBotActions() {
        ArrayList<BotActionObj> botActionList = super.loadBotActions();
        try {
            File actionsFile = getActionsFile();
            String jsonPath = SimpleGson.getInstance().toJson(botActionList);
            ReadWriteFile.writeFile(actionsFile, jsonPath);
        }
        catch (Exception ex){
            telemetry.addData("Error", "Unable to save Bot Actions. %s", ex.getMessage());
        }
        return botActionList;
    }


    private File getCoordinateFile(String filename)
    {
        String fullName = String.format("%s.json", filename);
        File file = new File(fullName);
        if (!file.isAbsolute())
        {
            AppUtil.getInstance().ensureDirectoryExists(DOTS_FOLDER);
            file = new File(DOTS_FOLDER, fullName);
        }
        return file;
    }

    private File getActionsFile()
    {
        return AppUtil.getInstance().getSettingsFile(BOT_ACTIONS);
    }

    private void initRoute(){
        if(newRoute != null){
            newRoute.getSteps().clear();
            newRoute = null;
        }
        newRoute = new AutoRoute();
        newRoute.setTemp(true);
        if (newRoute.getName().equals(NAME_BLUE)){
            newRoute.setNameIndex(getMinAvailableIndex(blueRoutes));
        }
        else{
            newRoute.setNameIndex(getMinAvailableIndex(redRoutes));
        }
        if (locator == null) {
            goToInstructions.setTargetX(startX);
            goToInstructions.setTargetY(startY);
        }
        else{
            goToInstructions.setTargetX((int)locator.getCurrentX());
            goToInstructions.setTargetY((int)locator.getCurrentY());
        }
    }


    protected void listRoutes(){
        try {
            int count = 0;
            AppUtil.getInstance().ensureDirectoryExists(ROUTES_FOLDER);
            File [] list = ROUTES_FOLDER.listFiles();
            if (list != null && list.length > 0) {
                for (final File rf : ROUTES_FOLDER.listFiles()) {
                    String jsonData = ReadWriteFile.readFile(rf);
                    AutoRoute route = AutoRoute.deserialize(jsonData);
                    if (count == 0) {
                        route.setSelected(true);
                        selectedRoute = route;
                    } else {
                        route.setSelected(false);
                    }
                    addRoute(route);
                    count++;
                }
            }
            //add New Route
            initRoute();
            addRoute(newRoute);
            selectedRoute = this.routes.get(0);
        }
        catch (Exception ex){
            telemetry.addData("Error listRoutes", ex.getMessage());
            telemetry.update();
        }
    }

    private void addRoute(AutoRoute route){
        this.routes.add(route);
        if (route.getName().equals(NAME_BLUE)){
            this.blueRoutes.add(route.getNameIndex());
        }
        else if  (route.getName().equals(NAME_RED)){
            this.redRoutes.add(route.getNameIndex());
        }
    }

    private int getMinAvailableIndex(ArrayList<Integer> list){
        int i = 1;
        for(int x = 0; x < list.size(); x++){
            if (list.get(x).equals(i)){
                i++;
            }
            else{
                break;
            }
        }
        return i;
    }

}
