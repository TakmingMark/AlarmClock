/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alarmclock.ServiceImplementations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.TimerTask;


import org.joda.time.DateTime;

import alarmclock.models.SetAlarm;
import alarmclock.testutils.Box;

/**
 * This class contains JUnit tests for the TimerAlarmStarter service implementation.
 * Since this class Does Something and is fairly complex, we need unit tests for it.
 * We make sure the unit tests are only testing this object by mocking or stubbing
 * any classes that this service relies upon.  This is why we use dependency injection.
 * @author Gordon
 */
public class TimerAlarmStarterTest {

    public TimerAlarmStarterTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of createAlarm method, of class TimerAlarmStarter.
     */
    @org.junit.Test
    public void testCreateAlarm() {
        System.out.println("CreateAlarm");
        //setup
        DateTime alarmDate = new DateTime();
        String exePath = "www.test.com";

        TimerAlarmStarter instance = new TimerAlarmStarter();

        //act
        SetAlarm result = instance.createAlarm(alarmDate, exePath);

        //assert
        assertEquals(new SetAlarm(alarmDate, exePath), result);
    }

    /**
     * Test of startAlarm method, of class TimerAlarmStarter.
     */
    @org.junit.Test
    public void testStartAlarm() throws Exception {
        System.out.println("StartAlarm");

        //setup
        SetAlarm alarm = new SetAlarm(new DateTime().plusMillis(100), "www.test.com");
        //we're using a box to hold the value because the box has to be final
        //so that we can reference it in the runnable
        final Box<Boolean> wasRun = new Box<Boolean>(false);

        Runnable whenFinished = new Runnable(){
            @Override
            public void run() {
                //set the value in the box
                wasRun.setValue(true);
            }
        };

        TimerAlarmStarter instance = new TimerAlarmStarter();

        //act
        TimerTask result = instance.startAlarm(alarm, whenFinished);

        //assert
        assertNotNull(result);
            //verify equality to within 10 milliseconds
        assertEquals(alarm.getTime().getMillis(), result.scheduledExecutionTime(), 10);
        assertFalse("value shouldn't get set till after timer goes off",
                wasRun.isValueSet());

        //wait out the alarm
        Thread.sleep(120);

        //verify the alarm was run
        wasRun.verifySet();
        assertTrue(wasRun.getValue());
    }

    @org.junit.Test
    public void testStartAlarm_InvalidTime_ThrowsException() throws Exception {
        System.out.println("testStartAlarm_InvalidTime_ThrowsException");

        //setup
        //set the alarm for in the past
        SetAlarm alarm = new SetAlarm(new DateTime().minusMillis(100), "www.test.com");

        //here's another way to test that the runnable was executed - use a mock
        Runnable whenFinished = mock(Runnable.class);

        TimerAlarmStarter instance = new TimerAlarmStarter();

        try
        {
            //act
            TimerTask result = instance.startAlarm(alarm, whenFinished);

            //assert
            //this is how you test that there should be an exception,
            //if an exception was thrown it should skip this line.
            fail("Should have thrown an exception");
        }
        catch(Exception e)
        {
            //we can perform assertions on the exception that was thrown.
            //here we are using a Hamcrest "is" matcher to assert that
            //the object is an instance of UnsupportedOperationException
            assertThat(e, is(UnsupportedOperationException.class));
        }

        //wait out the alarm
        Thread.sleep(120);

        //verify that the runnable was never executed
        verify(whenFinished, times(0)).run();
    }

    /**
     * Test of cancelAlarm method, of class TimerAlarmStarter.
     */
    @org.junit.Test
    public void testCancelAlarm() throws Exception {
        System.out.println("CancelAlarm");

        SetAlarm alarm = new SetAlarm(new DateTime().plusMillis(100), "www.test.com");
        final Box<Boolean> wasRun = new Box<Boolean>(false);

        Runnable whenFinished = new Runnable(){
            @Override
            public void run() {
                wasRun.setValue(true);
            }
        };

        TimerAlarmStarter instance = new TimerAlarmStarter();
        TimerTask result = instance.startAlarm(alarm, whenFinished);

        //act
        instance.cancelAlarm(alarm);

        //wait out the alarm
        Thread.sleep(120);

        //assert
        assertFalse("value should never have been set because the alarm was cancelled",
                wasRun.isValueSet());
    }

    @org.junit.Test
    public void testCancelByTimerTask() throws Exception {
        System.out.println("testCancelByTimerTask");

        //setup
        SetAlarm alarm = new SetAlarm(new DateTime().plusMillis(100), "www.test.com");
        final Box<Boolean> wasRun = new Box<Boolean>(false);

        Runnable whenFinished = new Runnable(){
            @Override
            public void run() {
                wasRun.setValue(true);
            }
        };

        TimerAlarmStarter instance = new TimerAlarmStarter();
        TimerTask result = instance.startAlarm(alarm, whenFinished);

        //act
        result.cancel();

        //wait out the alarm
        Thread.sleep(120);

        //assert
        assertFalse("value should never have been set because the alarm was cancelled",
                wasRun.isValueSet());
    }
}
