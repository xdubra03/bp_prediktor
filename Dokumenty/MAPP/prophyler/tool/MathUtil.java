/*
 * MathUtil.java
 *
 * Created on June 30, 2003, 12:40 PM
 */

package prophyler.tool;

import java.lang.*;
import java.util.*;
import prophyler.exception.*;

/** This is a static class that performs basic mathematical and statistical operations.
 * @author rkwok
 */
public class MathUtil {
    
    /** Creates a new instance of MathUtil. */
    public MathUtil() {
    }
    
    /** Given an array of numbers, calculates the sum of those numbers.
     * @param numArray An array of numbers (doubles).
     * @return The sum of the numbers in the array (as a double).
     */
    public static double getSum(double[] numArray) {
        double sum = 0;
        
        for (int i = 0; i < numArray.length; i++) {
            sum = sum + numArray[i];
        }
        
        return sum;
    }
    
    /** Calculates the square of a number.
     * @param number The number to be squared.
     * @return The squared number (as a double).
     */
    public static double getSquaredValue(double number) {
        double squaredValue = number * number;
        
        return squaredValue;
    }
    
    
    /** Takes an array of numbers and returns the average.
     * @param numArray An array of numbers (doubles).
     * @return The average of those numbers (as a double).
     * @throws MathException Throws MathException if the array has size 0.
     */
    public static double getAverage(double[] numArray) throws MathException {       
        int size = numArray.length;
        
        if (size == 0) {
            throw new MathException("Cannot compute average of empty set");
        }
        
        double sum = getSum(numArray);
        double average = sum / size;
        
        return average;
    }
    
    /** Takes an array of numbers and returns the median.  If the number of elements in the
     * array is odd, it returns the middle element.  If the number of elements if even, it
     * returns the mean of the middle two elements.
     * @param numArray An array of numbers (doubles).
     * @return The median of those numbers (as a double).
     */
    public static double getMedian(double[] numArray) throws MathException {
        double[] workingArray = new double[numArray.length];
        
        for (int i = 0; i < numArray.length; i++) {
            workingArray[i] = numArray[i];
        }

        Arrays.sort(workingArray);
        
        int size = workingArray.length;
        int midpoint = size / 2;
        double median;
        
        if ((size % 2) != 0) {
            median = workingArray[midpoint];
        }
        else {
            median = (workingArray[midpoint] + workingArray[midpoint-1]) / 2.0;
        }
        
        return median;
    }

    /** Takes an array of numbers and calculates the variance by summing the squared differences from
     * the average, dividing by the array size minus 1, and taking the square root.  If the user
     * wishes the variance to be corrected if it comes out as 0, they can specify a fudge factor which
     * will be added to the final variance result.
     * @param numArray An array of numbers (doubles).
     * @param average The average of those numbers (as a double).
     * @param fudgeFactor A number (double) that will be added to the final result.  If the user does
     * not wish any fudge factor to be added, specify 0 (not null).
     * @return The variance of the set of numbers (as a double).
     * @throws MathException Throws a MathException if the array size is 0 or 1, or if the final
     * calculated variance is negative (probably a result of a negative fudge factor).
     */    
    public static double getVariance(double[] numArray, double average, double fudgeFactor) throws MathException {
        return (Math.sqrt(getSquaredVariance(numArray, average, fudgeFactor)));
    }
    
    
    /** Takes an array of numbers and calculates the squared variance by summing the squared differences from
     * the average, and dividing by the array size minus 1.  If the user
     * wishes the variance to be corrected if it comes out as 0, they can specify a fudge factor which
     * will be added to the final variance result.
     * @param numArray An array of numbers (doubles).
     * @param average The average of those numbers (as a double).
     * @param fudgeFactor A number (double) that will be added to the final result.  If the user does
     * not wish any fudge factor to be added, specify 0 (not null).
     * @return The variance of the set of numbers (as a double).
     * @throws MathException Throws a MathException if the array size is 0 or 1, or if the final
     * calculated variance is negative (probably a result of a negative fudge factor).
     */
    public static double getSquaredVariance(double[] numArray, double average, double fudgeFactor) throws MathException {
        if (numArray.length <= 1) {
            throw new MathException("Cannot compute variance on set of size 0 or 1)");
        }
        
        int numElements = numArray.length;
        double[] squaredDifferencesList = new double[numElements];
        
        for (int i = 0; i < numElements; i++) {
            squaredDifferencesList[i] = getSquaredValue(numArray[i] - average);
        }
        
        double squaredVariance = (getSum(squaredDifferencesList) / (numElements - 1)) + fudgeFactor;
              
        if (squaredVariance < 0) {
            throw new MathException("Calculated negative variance");
        }
        
        return squaredVariance;
    }
    
    /** Takes two lists of doubles, one holding the numbers to be averaged, the second holding their
     * respective weights (ordered so that the index of the number is equal to the index of its weight).
     * Multiplies each number by its respective weight, and sums the products to produce the weighted
     * sum of all the numbers.
     * @param numList An array of the numbers to be averaged
     * @param weightList An array of the weights corresponding to the numbers
     * @return The weighted sum (as a double).
     * @throws MathException Throws a MathException if the numList is empty or if the size of numList
     * doesn't match the size of weightList
     */
    public static double getWeightedAverage(double[] numList, double[] weightList) throws MathException {
        double average = 0;
        double number;
        double weight;
        double weightedNum;
        
        if (numList.length == 0) {
            throw new MathException("Cannot compute average of empty set");
        }
        
        if (numList.length != weightList.length) {
            throw new MathException("Number of weights doesn't match number of numbers");
        }
        
        for (int i = 0; i < numList.length; i++) {
            number = numList[i];
            weight = weightList[i];

            weightedNum = number * weight;
            average = average + weightedNum;
        }

        return average;
    }
    
    /** Takes two lists, the first containing the numbers from which the variance will be calculated,
     * the other containing their respective weights (ordered so that the index of each number matches
     * the index of its weight).
     * Gets the squared difference from the average for each number, multiplies that by the number's
     * weight, and sums the weighted squared differences.  Then it calculates the sum of the weights
     * and subtracts that sum from 1.  It divides the weighted squared differences by the number
     * calculated in the previous sentence to get the weighted squared variance.  The user can specify
     * a fudge factor that will be added to the final squared variance result.
     * @param numList An array of the numbers from which the variance is to be calculated
     * @param weightList An array of the weights corresponding to the numbers
     * @param weightedAverage The weighted average of the numbers.
     * @param fudgeFactor A number to be added to the final variance result.  If the user does not
     * want to use a fudge factor, specify 0 (not null).
     * @return Returns the weighted squared variance (as a double).
     * @throws MathException Throws a MathException if the size of the set is 0, or the size of numList
     * doesn't match the size of weightList, or the final variance is negative (probably due to a
     * negative fudge factor).
     */
    public static double getWeightedSquaredVariance(double[] numList, double[] weightList, double weightedAverage, double fudgeFactor) throws MathException {
        double number;
        double weight;
        double weightedNum;
        int numElements = numList.length;
        
        if (numElements <= 1) {
            throw new MathException("Cannot compute variance on set of size 0 or 1");
        }
        
        if (numList.length != weightList.length) {
            throw new MathException("Number of weights doesn't match number of numbers");
        }        
        
        double[] squaredDifferencesList = new double[numElements];
        double[] squaredWeightsList = new double[numElements];
        
        for (int i = 0; i < numElements; i++) {
            number = numList[i];
            weight = weightList[i];
            squaredDifferencesList[i] = weight * (getSquaredValue(number - weightedAverage));
            squaredWeightsList[i] = getSquaredValue(weight);
        }
        
        double sumSquaredDifferences = getSum(squaredDifferencesList);
        double sumSquaredWeights = getSum(squaredWeightsList);
        double squaredVariance = (sumSquaredDifferences / (1 - sumSquaredWeights)) + fudgeFactor;
              
        if (squaredVariance < 0) {
            throw new MathException("Calculated negative variance");
        }
            
        return squaredVariance;
    }
    
    /** Does exactly the same thing as getWeightedSquaredVariance, but takes the square root at the
     * end.
     */
    public static double getWeightedVariance(double[] numList, double[] weightList, double weightedAverage, double fudgeFactor) throws MathException {
        return (Math.sqrt(getWeightedSquaredVariance(numList, weightList, weightedAverage, fudgeFactor)));
    }
    
    /** Subtracts the average from the number and divides by the variance.
     */
    public static double standardize(double number, double average, double variance) throws MathException {
        if (variance == 0) {
            throw new MathException("Cannot divide by zero variance");
        }
        
        double standardizedNumber = (number - average) / variance;
        
        return standardizedNumber;
    }

    /** Calculates the average and the variance of the number array (using the fudge factor if
     * specified), then returns an array of the standardized numbers.  Numbers are standardized by
     * subtracting the average and dividing by the variance.
     */
    public static double[] standardize(double[] numbers, double varianceFudgeFactor) throws MathException {
        double[] standardizedNumbers = new double[numbers.length];
        double standardizedNum;
        
        double average = getAverage(numbers);
        double variance = getVariance(numbers, average, varianceFudgeFactor);
        
        for (int i = 0; i < numbers.length; i++) {
            standardizedNum = standardize(numbers[i], average, variance);
            standardizedNumbers[i] = standardizedNum;
        }
        
        return standardizedNumbers;
    }
    
    /** For each row in the 2-D array, calculates the average and variance, and then standardizes the
     * numbers in that row.
     */
    public static double[][] standardize(double[][] numbers, double varianceFudgeFactor) throws MathException {
        double[] firstRow = numbers[0];
        double[][] standardizedNumbers = new double[numbers.length][firstRow.length];
        double[] row;
        double[] standardizedRow;
        
        for (int i = 0; i < numbers.length; i++) {
            row = numbers[i];
            standardizedRow = standardize(row, varianceFudgeFactor);
            standardizedNumbers[i] = standardizedRow;
        }
        
        return standardizedNumbers;
    }
    
    /** Returns the same number array with each element square-rooted.
     */
    public static double[] sqrt(double[] numArray) {
        double num;
        double[] newNumArray = new double[numArray.length];
        
        for (int i = 0; i < numArray.length; i++) {
            num = numArray[i];
            newNumArray[i] = Math.sqrt(num);
        }
        
        return newNumArray;
    }
    
    /** Returns the same number array with each element inverse-square-rooted.
     */
    public static double[] invSqrt(double[] numArray) {
        double num;
        double[] newNumArray = new double[numArray.length];
        
        for (int i = 0; i < numArray.length; i++) {
            num = numArray[i];
            newNumArray[i] = 1.0 / (Math.sqrt(num));
        }
        
        return newNumArray;
    }    

    /** Smooths an array of numbers with a given window size.
      * @throws MathException If the window size is not a positive odd number
      */
    public static double[] smooth(double[] numbers, int window) throws MathException {
        double[] smoothedNumbers = new double[numbers.length];
        double number;
        double[] surroundingNumbers;
        double smoothedNumber;
        
        if ((window < 1) || (window % 2 == 0)) {
            throw new MathException("Must specify positive odd number for window size");
        }
        
        if (window == 1) {
            smoothedNumbers = numbers;
            return smoothedNumbers;
        }
        
        int midpoint = window / 2;

        for (int i = midpoint; i < (numbers.length-midpoint); i++) {
            number = numbers[i];
            surroundingNumbers = getSubArray(numbers, i-midpoint, i+midpoint);
            smoothedNumber = getAverage(surroundingNumbers);
            smoothedNumbers[i] = smoothedNumber;
        }
        
        return smoothedNumbers;
    }

    /* Given an array of numbers, a starting index, and an ending index, returns a
     * new sub-array containing the numbers from the start to end index, in the same
     * order as the original array.
     * @throws MathException If the start or end index is negative or greater than the
     * array size
     */
    public static double[] getSubArray(double[] numbers, int start, int end) throws MathException {
        if ((start < 0) || (end < 0)) {
            throw new MathException("Start and end indices cannot be negative");
        }   
        
        if (end > (numbers.length-1)) {
            throw new MathException("Cannot specify end index greater than array size");
        }
        
        double[] subArray = new double[end-start+1];
        int index = 0;
        
        for (int i = start; i <= end; i++) {
            subArray[index] = numbers[i];
            index++;
        }
        
        return subArray;
    }   
}
