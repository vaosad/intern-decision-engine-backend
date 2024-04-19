## Implementation

In the implemented code, several features have been added or modified to enhance the decision-making process for loan 
approval based on customer's age.
1. Added two constants in DecisionEngineConstants. MINIMUM_ALLOWED_AGE (represents the age of majority) and 
MAXIMUM_ALLOWED_AGE (represents the expected lifetime of a country).
2. A method named calculateAge has been added to determine the age of the customer based on their 
personal code. This method extracts the birth year, month, and day from the personal code and calculates the age by 
comparing the birthdate with the current date.
3. Added a method named getCentury which extracts the century from the first digit of the year in a personal code. 
4. Added a method named validateAge which validates the age of a customer against predefined constraints (of class 
DecisionEngineConstants) to determine eligibility for a loan. 
5. Modified calculateApprovedLoan method to use the methods.

## Vision of improvement 

1. As of now, I assumed that each Baltic country has the same expected lifetime. However, this might not be the case in 
reality. Additionally, there is no way to obtain information about the country a certain person is from (especially if 
we temporarily assume that the personal code is the same for every country). This forced me to resort to the solution I 
presented to you. Ideally, if both the frontend and backend requested an additional parameter, such as the country the 
person is from, I could have stored the information about the countries and their expected lifespans in a map instead 
of using constants, and used it accordingly. The keys of the map would represent country codes (e.g., "EST" for 
Estonia), and the values would represent the expected lifespans in years. Then, in the calculateAge method, I would 
determine the expected lifetime of the country based on the provided country code. This modification would allow the 
code to be more flexible and adaptable to different expected lifespans for different countries. The map could be easily 
extended with additional country codes and expected lifespans as needed.
2. I have written four tests to check the functionality of the TICKET-102 methods. They are assumed to be temporary, 
just to check. As time passes, the provided personal codes for the tests will not work as intended, and the tests will 
fail. Of course, if there was a need to keep them for a longer time, than this year, would be great to modify them to 
keep up with the times.