package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.EditText;

import com.freeteam01.androidnewsgroupreader.Services.RuntimeStorage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    static String testEmail = "forename.surname@email.com";
    static String testSurename = "Surname";
    static String testForename = "Forename";

    @Rule
    public ActivityTestRule mainActivityRule = new ActivityTestRule<>(MainActivity.class);
//    @Rule
//    public ActivityTestRule subscribeActivityRule = new ActivityTestRule<>(SubscribeActivity.class);
    //@Rule
    //public ActivityTestRule postActivityRule = new ActivityTestRule<>(PostActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.freeteam01.androidnewsgroupreader", appContext.getPackageName());
    }

    @Test
    public void subscribeTest() throws Exception {
        onView(isRoot()).perform(ViewActions.pressMenuKey());
        onView(withText(R.string.subscribe)).perform(click());
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText("test"), pressKey(KeyEvent.KEYCODE_ENTER));
        onData(anything()).inAdapterView(withId(R.id.lv_newsgroups)).atPosition(0).perform(click());
        onView(withId(R.id.btn_save)).perform(click());
        Espresso.pressBack();
    }

    @Test
    public void sortTest() throws Exception {
        for (int i = 1; i < 4; i++) {
            onView(withId(R.id.spinner_sort)).perform(click());
            onData(anything()).atPosition(i % 3).perform(click());
        }
    }

    @Test
    public void searchTest() throws Exception {
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText("test"), pressKey(KeyEvent.KEYCODE_ENTER));
        sortTest();
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
    public void mainActivityScrollPost() throws Exception {
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
        onView(withId(R.id.tv_article)).perform(swipeUp());
        onView(withId(R.id.tv_article)).perform(swipeDown());
    }

    @Test
    public void viewAnsweredArticle() {
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
        onData(anything()).inAdapterView(withId(R.id.tree_view))
                .atPosition(0).perform(click());
    }

    @Test
    public void answerArticle() {
        RuntimeStorage.instance().getUserSetting().setForename("");
        onData(anything()).inAdapterView(withId(R.id.treeList))
                .atPosition(0).perform(click());
        onView(withId(R.id.btn_answer_article)).perform(click());
        onView(withId(R.id.email)).perform(replaceText(testEmail));
        onView(withId(R.id.forename)).perform(replaceText(testForename));
        onView(withId(R.id.surname)).perform(replaceText(testSurename));
        onView(withId(R.id.save_settings_button)).perform(click());
        onView(withId(R.id.btn_answer_article)).perform(click());
        onView(withId(R.id.et_subject)).perform(click());
        onView(withId(R.id.et_subject)).perform(replaceText("Another test"));
        onView(withId(R.id.et_post)).perform(click());
        onView(withId(R.id.et_post)).perform(replaceText("Another test"));
//        onView(withId(R.id.btn_article_send)).perform(click());
    }

    @Test
    public void newArticle() {
        RuntimeStorage.instance().getUserSetting().setForename("");
        onView(withId(R.id.btn_add_article)).perform(click());
        onView(withId(R.id.email)).perform(replaceText(testEmail));
        onView(withId(R.id.forename)).perform(replaceText(testForename));
        onView(withId(R.id.surname)).perform(replaceText(testSurename));
        onView(withId(R.id.save_settings_button)).perform(click());
        onView(withId(R.id.btn_add_article)).perform(click());
        onView(withId(R.id.et_subject)).perform(click());
        onView(withId(R.id.et_subject)).perform(replaceText("Another test"));
        onView(withId(R.id.et_post)).perform(click());
        onView(withId(R.id.et_post)).perform(replaceText("Another test"));
//        onView(withId(R.id.btn_article_send)).perform(click());
    }

    @Test
    public void logout() {
        onView(isRoot()).perform(ViewActions.pressMenuKey());
        onView(withText(R.string.logout)).perform(click());
        assertTrue(mainActivityRule.getActivity().isFinishing());
    }
}