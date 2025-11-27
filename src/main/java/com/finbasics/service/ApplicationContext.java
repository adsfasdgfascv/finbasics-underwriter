package com.finbasics.service;

/**
 * Small static holder for the "currently opened" application
 * when navigating from Dashboard -> Applicant Detail.
 */
public class ApplicationContext {

    private static Integer currentApplicationId;

    public static Integer getCurrentApplicationId() {
        return currentApplicationId;
    }

    public static void setCurrentApplicationId(Integer id) {
        currentApplicationId = id;
    }
}
