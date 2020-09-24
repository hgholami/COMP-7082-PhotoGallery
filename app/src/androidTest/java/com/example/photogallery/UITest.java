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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.keywordField)).check(matches(isDisplayed()));
        onView(withId(R.id.keywordField)).perform(typeText("e"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(isDisplayed()));
    }
    @Test
    public void searchDateTest() {
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.keywordField)).check(matches(isDisplayed()));
        onView(withId(R.id.startDateField)).perform(typeText("2019-10-10"), closeSoftKeyboard());
        onView(withId(R.id.endDateField)).perform(typeText("2025-10-10"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(isDisplayed()));
    }

}