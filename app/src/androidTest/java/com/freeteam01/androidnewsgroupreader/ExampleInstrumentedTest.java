package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityTestRule mainActivityRule = new ActivityTestRule<>(MainActivity.class);
    @Rule
    public ActivityTestRule subscribeActivityRule = new ActivityTestRule<>(SubscribeActivity.class);
    //@Rule
    //public ActivityTestRule postActivityRule = new ActivityTestRule<>(PostActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.freeteam01.androidnewsgroupreader", appContext.getPackageName());
    }

    @Test
    public void startSubscribeDialogSW() throws Exception {
        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.subscribe)).perform(click());
        onView(withContentDescription("Navigate up")).perform(click());
    }

    @Test
    public void startSubscribeDialogHW() throws Exception {
        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.subscribe)).perform(click());
        pressBack();
    }

    @Test
    public void mainActivitySubscribedNewsGroupSpinner() throws Exception {
        onData(anything()).inAdapterView(withId(R.id.newsgroups_spinner))
                .atPosition(0).perform(click());
    }

    @Test
    public void startPostActivity() throws Exception {
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
    }

    @Test
    public void postActivityClickTree() throws Exception {
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
        onData(anything()).inAdapterView(withId(R.id.tree_view))
                .atPosition(0).perform(click());
    }

    @Test
    public void postActivityScrollTree() throws Exception {
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
        onData(anything()).inAdapterView(withId(R.id.tree_view))
                .atPosition(0).perform(swipeUp());
        onData(anything()).inAdapterView(withId(R.id.tree_view))
                .atPosition(0).perform(swipeDown());
    }

    @Test
    public void postActivityScrollPost() throws Exception {
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
        onView(withId(R.id.tv_article)).perform(swipeUp());
        onView(withId(R.id.tv_article)).perform(swipeDown());
    }

}
