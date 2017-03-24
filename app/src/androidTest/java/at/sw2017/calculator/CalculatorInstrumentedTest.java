package at.sw2017.calculator;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CalculatorInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("at.sw2017.calculator", appContext.getPackageName());
    }

    @Rule
    public ActivityTestRule<Calculator> mActivityRule = new ActivityTestRule<>(Calculator.class);

    @Test
    public void testButtons() throws Exception {
        for (int i = 0; i <= 9; i++) {
            onView(withText(Integer.toString(i))).perform(click());
        }
        onView(withText("+")).perform(click());
        onView(withText("-")).perform(click());
        onView(withText("*")).perform(click());
        onView(withText("/")).perform(click());
        onView(withText("=")).perform(click());
        onView(withText("C")).perform(click());
    }

    @Test
    public void testInputField() {
        for (int i = 9; i >= 0; i--) {
            onView(withText(Integer.toString(i))).perform(click());
        }
        onView(withText("9876543210")).check(matches(isDisplayed()));
    }

    @Test
    public void testClearButton() {
        onView(withText("3")).perform(click());
        onView(withText("C")).perform(click());
        onView(withId(R.id.textView)).check(matches(withText("0")));
    }

    @Test
    public void testAdd() {
        onView(withText("1")).perform(click());
        onView(withText("+")).perform(click());
        onView(withText("2")).perform(click());
        onView(withText("=")).perform(click());
        onView(withId(R.id.textView)).check(matches(withText("3")));
    }

    @Test
    public void testSub() {
        onView(withText("1")).perform(click());
        onView(withText("-")).perform(click());
        onView(withText("2")).perform(click());
        onView(withText("=")).perform(click());
        onView(withId(R.id.textView)).check(matches(withText("-1")));
    }

    @Test
    public void testMul() {
        onView(withText("3")).perform(click());
        onView(withText("*")).perform(click());
        onView(withText("7")).perform(click());
        onView(withText("=")).perform(click());
        onView(withId(R.id.textView)).check(matches(withText("21")));
    }

    @Test
    public void testSimpleEqual() {
        onView(withText("5")).perform(click());
        onView(withText("=")).perform(click());
        onView(withId(R.id.textView)).check(matches(withText("5")));
    }

    @Test
    public void testOnlyEqual() {
        onView(withText("=")).perform(click());
        onView(withId(R.id.textView)).check(matches(withText("0")));
    }

    @Test
    public void testValueOfState() {
        Calculator.State.valueOf(Calculator.State.ADD.toString());
    }
}
