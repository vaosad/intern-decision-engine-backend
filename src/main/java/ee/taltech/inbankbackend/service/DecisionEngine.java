package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException {
        try {
            verifyInputs(personalCode, loanAmount, loanPeriod);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        int age = calculateAge(personalCode);
        validateAge(age);

        int outputLoanAmount;
        creditModifier = getCreditModifier(personalCode);

        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        double creditScore = ((double) creditModifier / loanAmount) * loanPeriod;

        while (creditScore < 1.0) {

            if (loanAmount > DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
                loanAmount -= 100;
            } else if (loanPeriod < DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
                loanPeriod++;
            } else {
                throw new NoValidLoanException("No valid loan found!");
            }

            creditScore = ((double) creditModifier / loanAmount) * loanPeriod;
        }

        outputLoanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, highestValidLoanAmount(loanPeriod));

        return new Decision(outputLoanAmount, loanPeriod, null);
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    private int highestValidLoanAmount(int loanPeriod) {
        return creditModifier * loanPeriod;
    }

    /**
     * Validates the age of a customer against predefined constraints to determine eligibility for a loan.
     *
     * @param age The age of the customer to be validated.
     * @throws NoValidLoanException If the age does not meet the eligibility criteria for a loan.
     */
    private void validateAge(int age) throws NoValidLoanException {
        if (age < DecisionEngineConstants.MINIMUM_ALLOWED_AGE ||
                age > (DecisionEngineConstants.MAXIMUM_ALLOWED_AGE - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD / 12)) {
            throw new NoValidLoanException("Customer is ineligible for a loan due to age constraints.");
        }
    }

    /**
     * Calculates the age of a person based on their personal code, which typically includes information about their birthdate.
     *
     * @param personalCode The personal code containing information about the person's birthdate.
     * @return The calculated age of the person.
     * @throws InvalidPersonalCodeException If the provided personal code is invalid/cannot be parsed.
     */
    private int calculateAge(String personalCode) throws InvalidPersonalCodeException {

        int birthYear = Integer.parseInt(personalCode.substring(1, 3));
        int birthMonth = Integer.parseInt(personalCode.substring(3, 5));
        int birthDay = Integer.parseInt(personalCode.substring(5, 7));

        int century = getCentury(personalCode.charAt(0));

        LocalDate birthDate = LocalDate.of(century + birthYear, birthMonth, birthDay);
        LocalDate currentDate = LocalDate.now();

        int age = currentDate.getYear() - birthDate.getYear();

        // Adjusting age if birthday hasn't occurred yet this year
        if (currentDate.getMonthValue() < birthDate.getMonthValue()
                || (currentDate.getMonthValue() == birthDate.getMonthValue()
                && currentDate.getDayOfMonth() < birthDate.getDayOfMonth())) {
            age--;
        }

        return age;
    }

    /**
     * Extracts the century from the first digit of the year in a personal code.
     *
     * @param firstDigitOfYear The first digit of the year in the personal code.
     * @return The century corresponding to the provided first digit of the year.
     * @throws InvalidPersonalCodeException If the provided first digit of the year is not recognized.
     */
    private int getCentury(char firstDigitOfYear) throws InvalidPersonalCodeException {
        int firstDigit = Character.getNumericValue(firstDigitOfYear);

        if (firstDigit == 3 || firstDigit == 4) {
            return 1900;
        } else if (firstDigit == 5 || firstDigit == 6) {
            return 2000;
        } else {
            throw new InvalidPersonalCodeException(
                    String.format("Unknown first digit %d in the personal code", firstDigit));
        }
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
