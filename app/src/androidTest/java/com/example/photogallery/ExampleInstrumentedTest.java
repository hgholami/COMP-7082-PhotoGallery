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
        import static androidx.test.espresso.action.ViewActions.typeText;
        import static androidx.test.espresso.assertion.ViewAssertions.matches;
        import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
        import static androidx.test.espresso.matcher.ViewMatchers.withId;
        import static androidx.test.espresso.matcher.ViewMatchers.withText;
        import static org.hamcrest.core.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleInstrumentedTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void searchCaptionTest() {
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.keywordField)).check(matches(isDisplayed()));
        onView(withId(R.id.keywordField)).perform(typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));//should be 2
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.keywordField)).perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.rightButton)).perform(click());//1
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("1"))));
        onView(withId(R.id.rightButton)).perform(click());//2
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.rightButton)).perform(click());//3
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("3"))));
        onView(withId(R.id.leftButton)).perform(click());//2
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.leftButton)).perform(click());//1
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("1"))));
        onView(withId(R.id.leftButton)).perform(click());//4
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("4"))));
    }
    @Test
    public void searchDateTest() {
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.keywordField)).check(matches(isDisplayed()));
        onView(withId(R.id.startDateField)).perform(typeText("2019-10-10"), closeSoftKeyboard());
        onView(withId(R.id.endDateField)).perform(typeText("2025-10-10"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("4"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("1"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("3"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("1"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("4"))));
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.startDateField)).perform(typeText("2019-10-10"), closeSoftKeyboard());
        onView(withId(R.id.endDateField)).perform(typeText("2019-10-11"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString(""))));
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.startDateField)).perform(typeText("2019-10-10"), closeSoftKeyboard());
        onView(withId(R.id.endDateField)).perform(typeText("2025-10-10"), closeSoftKeyboard());
        onView(withId(R.id.searchButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("4"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("1"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.rightButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("3"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("2"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("1"))));
        onView(withId(R.id.leftButton)).perform(click());
        onView(withId(R.id.editCaption)).check(matches(withText(containsString("4"))));
    }

}