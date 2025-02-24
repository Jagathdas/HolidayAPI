# HolidayAPI
Holiday API - Spring Boot Application
This is a Spring Boot application that provides a RESTful API for managing holiday data. You can use this API to view, add, update, delete, and upload holiday records.

Prerequisites
JDK 17+ (or compatible version)
Maven or Gradle (for building the application)
Spring Boot 3.4.2 or compatible version
IDE like IntelliJ IDEA, Eclipse, or Spring Tool Suite (STS)
Postman or cURL to test the API endpoints
Running the Application
1. Clone or Download the Application
Clone or download this repository to your local machine:

bash
Copy
git clone <repository_url>
2. Build and Run the Application
If you're using Maven, navigate to the project directory in the terminal and run:

bash
Copy
mvn spring-boot:run
If you're using Gradle, run the following command:

bash
Copy
./gradlew bootRun
By default, the application will run on http://localhost:8080.

API Endpoints
1. Get Holidays for a Specific Country
URL: GET /api/holidays?country={country}
Description: Retrieves a list of holidays for a given country.
Request Parameters:
country (required): The country for which holidays need to be fetched.
Example Request:
bash
Copy
GET http://localhost:8080/api/holidays?country=USA
Example Response:
json
Copy
[
    {
        "id": 1,
        "country": "USA",
        "name": "New Year's Day",
        "date": "2025-01-01"
    },
    {
        "id": 2,
        "country": "USA",
        "name": "Independence Day",
        "date": "2025-07-04"
    }
]
2. Add a New Holiday
URL: POST /api/holidays
Description: Adds a new holiday for a given country.
Request Body:
country (required): The country to which the holiday belongs.
name (required): The name of the holiday.
date (required): The date of the holiday (in yyyy-MM-dd format).
Example Request:
bash
Copy
POST http://localhost:8080/api/holidays
Content-Type: application/json

{
  "country": "USA",
  "name": "Christmas Day",
  "date": "2025-12-25"
}
Example Response:
json
Copy
{
    "id": 3,
    "country": "USA",
    "name": "Christmas Day",
    "date": "2025-12-25"
}
3. Update an Existing Holiday
URL: PUT /api/holidays/{id}
Description: Updates an existing holiday by its ID.
Request Parameters:
id (required): The ID of the holiday to be updated.
Request Body:
country (required): The country of the holiday.
name (required): The updated name of the holiday.
date (required): The updated date of the holiday (in yyyy-MM-dd format).
Example Request:
bash
Copy
PUT http://localhost:8080/api/holidays/3
Content-Type: application/json

{
  "country": "USA",
  "name": "Christmas Eve",
  "date": "2025-12-24"
}
Example Response:
json
Copy
{
    "id": 3,
    "country": "USA",
    "name": "Christmas Eve",
    "date": "2025-12-24"
}
4. Delete a Holiday by ID
URL: DELETE /api/holidays/{id}
Description: Deletes a holiday by its ID.
Request Parameters:
id (required): The ID of the holiday to be deleted.
Example Request:
bash
Copy
DELETE http://localhost:8080/api/holidays/3
Example Response:
json
Copy
{
    "message": "Holiday deleted successfully"
}
5. Upload a File Containing Holiday Data in CSV Format
URL: POST /api/holidays/upload
Description: Uploads a CSV file containing holiday data.
Request Parameter:
file (required): A CSV file containing holiday data. The file should have the format:

pgsql
Copy
Country,Name,Date
Example CSV file content:

csv
Copy
USA,New Year's Day,2025-01-01
USA,Independence Day,2025-07-04
Example Request (using Postman):
Select POST method.
URL: http://localhost:8080/api/holidays/upload
In the Body, select form-data and choose a CSV file as the value for the file field.
Example Response:
json
Copy
{
    "message": "File uploaded and holidays added successfully!"
}
Error Handling
In case of errors, the API will respond with appropriate HTTP status codes and error messages:

400 Bad Request: If the request is invalid (e.g., missing required fields).
404 Not Found: If a holiday or country is not found.
500 Internal Server Error: If something goes wrong on the server side (e.g., file upload errors).
Example of error response:

json
Copy
{
    "error": "Holiday not found with ID: 3"
}
Testing the API
You can use Postman, cURL, or any other API testing tool to test the endpoints.

For example, to test the GET endpoint using cURL:

bash
Copy
curl -X GET "http://localhost:8080/api/holidays?country=USA"
To test the POST endpoint to add a new holiday:

bash
Copy
curl -X POST "http://localhost:8080/api/holidays" -H "Content-Type: application/json" -d '{
  "country": "USA",
  "name": "Labor Day",
  "date": "2025-09-01"
}'



Swagger UI URL - http://localhost:8080/swagger-ui/index.html#/