package com.example.photogallery;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void searchCaptionTest() {
        onView(withId(R.id.editCaption)).perform(replaceText("eeeeeeeeeeeeeeeeeeeeeeeaaaaaa"), closeSoftKeyboard());
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.keywordField)).perform(typeText("e"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("e"))));
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("eeeeea"))));
    }

}