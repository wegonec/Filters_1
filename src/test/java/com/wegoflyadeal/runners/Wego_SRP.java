package com.wegoflyadeal.runners;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;


import com.wegoflyadeal.api.WegoApliClient;
import com.wegoflyadeal.constants.Constants;
import com.wegoflyadeal.helpers.WegoFlights;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Wego_SRP {

	WebDriver driver;
	public static List<WegoFlights> WegoFlightsVar = new ArrayList<WegoFlights>();
	int PaymenttypesLoop = 1;

	String win_value = "0";
	String Destination = "";
	String Source = "";
	String win_decision = "";
	String Airline = "";
	String DepartDate = "";
	String DepartTime = "";
	String CurrencyCode = "";
	String TripType = "";
	String APIPrice = "";
	String Client = "";
	String Provider = "";
	String ThresholdType = "";
	String ThresholdValue = "";
	String NetChange = "0";
	String Domain = "";
	String DiffPrice = "";
	String FltNum ="";

	boolean isWegoStarted = false;
	String PgFee = "";
	String Lead = "";

	boolean isAirline = false;

	String[] alphaRoutes = {"JED-TUU", "RUH-DMM", "RUH-MED", "TUU-JED"};

	String SystemName = "F3 - SA WEGO Scrapper - System Alpha";

	int flightRunCount = 0;
	String Wego_URL;

	public Set<String> completedRouteTime = new HashSet<>();

	public Set<String> completedFlightNumbers = new HashSet<>();

	@AfterTest
	public void quitTheSession() {
		if (driver != null) {
			driver.quit();
		}
	}

	@BeforeTest
	public void setUp() throws InterruptedException {
		FirefoxOptions options = new FirefoxOptions();
		options.addPreference("layout.css.devPixelsPerPx", "0.3");
		options.addPreference("permissions.default.image", 2);
		options.addArguments("--headless");
		driver = new FirefoxDriver(options);
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		driver.manage().deleteAllCookies();
	    //wego_Login();
	}

   
    @SuppressWarnings("unchecked")
    @Test(priority = 0)
    public void readAllJson() throws ClientProtocolException, IOException, ParseException {
    	
    	 LocalTime currentTime = LocalTime.now();
    	LocalTime startTime = LocalTime.of(23, 30); // 11 PM
        LocalTime endTime = LocalTime.of(5, 30); // 5 AM
        
        if ((currentTime.isAfter(startTime) && currentTime.isBefore(LocalTime.MIDNIGHT)) ||
                (currentTime.isAfter(LocalTime.MIDNIGHT) && currentTime.isBefore(endTime)) ||
                currentTime.equals(startTime) || currentTime.equals(endTime)) 
               {
        	       performApiCall(Constants.Get_API_Path1);
        	       
        		} else {
        			
        			performApiCall(Constants.Get_API_Path1);
        		}
        	//performApiCall(Constants.Get_API_Path2);
        	
           
            
        
    }

    private void performApiCall(String apiPath) throws ClientProtocolException, IOException, ParseException {
        System.err.println("URL : " + apiPath);
        CloseableHttpResponse closeableHttpResponse = WegoApliClient.get(apiPath);
        String response = EntityUtils.toString(closeableHttpResponse.getEntity(), "utf-8");

        // Log the response for debugging
        //System.out.println("API Response: " + response);

        try {
            if (response.trim().startsWith("[")) {
                // Handle JSONArray
                org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                //System.out.println("JSONArray: " + jsonArray.toString(2));

                // Save JSONArray to file
                JSONObject obj = new JSONObject();
                obj.put("Wego", jsonArray);
                Files.write(Paths.get(Constants.JSON_RESULTFILE_PATH), obj.toJSONString().getBytes());
            } else if (response.trim().startsWith("{")) {
                // Handle JSONObject
                org.json.JSONObject jsonObject = new org.json.JSONObject(response);
                //System.out.println("JSONObject: " + jsonObject.toString(2));

                // Save JSONObject to file
                Files.write(Paths.get(Constants.JSON_RESULTFILE_PATH), response.getBytes());
            } else {
                throw new JSONException("Unexpected response format: " + response);
            }

            // Process data further if required
            getDataFromApiResponse();
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("Error parsing the API response as JSON.");
        }
    }
	
	public void getDataFromApiResponse() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		try {
			FileReader reader = new FileReader(Constants.JSON_RESULTFILE_PATH);
			Object obj = jsonParser.parse(reader);
			JSONObject hotelsList = (JSONObject) obj;
			org.json.simple.JSONArray responseList = (org.json.simple.JSONArray) hotelsList.get("Wego");
			//System.out.println("Size of Json - " + responseList.size());

			int totalSize = responseList.size();
			System.out.println("totalSize : " + totalSize);
			
			/*for (int i = 0; i < responseList.size(); i++) {
	            try {
	                JSONObject details = (JSONObject) responseList.get(i);
	                //System.out.println("------ Record " + (i + 1) + " ------");
	                System.out.println("domain: " + details.get("domain"));
	                System.out.println("source: " + details.get("source"));
	                System.out.println("destination: " + details.get("destination"));
	                System.out.println("tripType: " + details.get("tripType"));
	                System.out.println("airline: " + details.get("airline"));
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }*/
			for (int i = responseList.size() - 1; i >= 0; i--) {
			//for (int i = 0; i < responseList.size(); i++) {
				try {
					JSONObject details = (JSONObject) responseList.get(i);
					String source = (String) details.get("source");
					Source = source;
					String destination = (String) details.get("destination");
					Destination = destination;
					String DepartureDate = (String) details.get("departDate").toString();
					DepartDate = DepartureDate;
					String airline = (String) details.get("airline");
					Airline = airline;

					ThresholdType = (String) details.get("thresholdType");
					Client = (String) details.get("client");
					Domain = (String) details.get("domain");
					TripType = (String) details.get("tripType");
					ThresholdValue = String.valueOf(details.get("thresholdValue"));

					Long PgFeeLong = (Long) details.get("pgFee");
					PgFee = String.valueOf(PgFeeLong).trim();
					Lead = String.valueOf(details.get("lead"));

					isAirline = (boolean) details.get("isAirline");

					//System.out.println("----------------------------------------------------");
					/*System.out.println("Record Number :" + (i + 1));
					System.out.println("Source : " + source);
					System.out.println("Destination : " + destination);
					System.out.println("DepartureDate : " + DepartureDate);
					System.out.println("TripType : " + TripType);
					System.out.println("PgFee : " + PgFee);
					System.out.println("Lead : " + Lead);
					System.out.println("isAirline : " + isAirline);*/

					String[] DateSplits = DepartureDate.split("T")[0].split("-");

					String monthOfDate = DateSplits[1].trim();
					String dayOfDate = DateSplits[2].trim();
					if (dayOfDate.startsWith("0")) { 
						dayOfDate = dayOfDate.replace("0", "");
					}
					//System.out.println("Airline : " + airline);

					String apiRoute = source.trim() + "-" + destination.trim();

					if (airline.toLowerCase().trim().contains("f3")) {
						for (String routeGamma : alphaRoutes) {  
							if (routeGamma.equalsIgnoreCase(apiRoute)) {
								System.out.println("Record Number :" + (i + 1));
								System.out.println("Source : " + source);
								System.out.println("Destination : " + destination);
								System.out.println("DepartureDate : " + DepartureDate);
								System.out.println("TripType : " + TripType);
								System.out.println("PgFee : " + PgFee);
								System.out.println("Lead : " + Lead);
								System.out.println("isAirline : " + isAirline);
								System.out.println("I value : " + i);
								search(source, destination, DepartureDate.split("T")[0], monthOfDate, dayOfDate,airline);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			/*if (isWegoStarted) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date enddate = new Date();
				String dateEnded = dateFormat.format(enddate);
				System.out.println(dateEnded);
				CommonUtility.sendMail("automation@rehlat.com","qateam@rehlat.com", SystemName+" - Completed "+dateEnded+" ", "Dear All,\n\n Please note that "+SystemName+" has successfully completed at"+dateEnded+"\n"+Arrays.asList(alphaRoutes)+"\n\n Thanks, \n Gopi");

			}*/
		} catch (Exception e) {
		}

		System.out.println("flightRunCount : " + flightRunCount);

		System.out.println("flightRunCount : " + completedFlightNumbers);
	}

	String websiteName = "sa.wego.com";

	// com wego = kw.wego.com rehlat.com
	// ae wego = www.wego.ae our site rehlat.ae
	// sa wego = sa.wego.com our site rehlat.com.sa
	// eg wego = eg.wego.com our site rehlat.com.eg
	private Set<String> visitedURLs = new HashSet<>();

	public void search(String source, String destination, String date, String DepartureMonth, String DepartureDay, String airline) throws InterruptedException {
		
	    //Wego_URL = "https://"+websiteName+"/en/flights/searches/"+source+"-"+destination+"-"+ date+"/economy/1a:0c:0i?sort=price&order=asc&airlines=F3%2CXY";

	    Wego_URL = "https://"+websiteName+"/en/flights/searches/"+source+"-"+destination+"-"+ date+"/economy/1a:0c:0i?sort=leg1_departure_time&order=desc&ulang=en&placement_type=integrated_booking&payment_methods=10%2C15%2C152%2C189%2C11%2C9%2C16%2C13%2C3";

	    // Check if the URL has been visited before
	    if (visitedURLs.contains(Wego_URL)) {
	        System.out.println("Duplicate URL detected - Skipping search");
	        return; // Skip the search if the URL is a duplicate
	    }

	    // Visit the URL and add it to the set of visited URLs
	    driver.get(Wego_URL);
	    System.out.println(Wego_URL);

	    try {
	        Thread.sleep(5000);
	    } catch (InterruptedException e) {
	    }
	    resultsForWego();
	    Next_Dates();
	    // Add the current URL to the set of visited URLs
	    visitedURLs.add(Wego_URL);
	}
	
	public void resultsForWego() throws InterruptedException {
        int maxRetries = 2;
        boolean isPageLoaded = false; 

        for (int retryCount = 1; retryCount <= maxRetries; retryCount++) {
            try {
            	
            	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5)); // 10 seconds timeout
                WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Stops')]")));
                System.out.println("Wego SRP Page Displayed");
				try {
					WebElement checkbox = driver.findElement(By.xpath("//div[contains(@class, 'HjHhQ5P5R0Q0aCiTlV7G') and contains(@class, 'Ia5JDEF_0rL4Gh4_fLO7')]"));
					checkbox.click();
					String originalWindow = driver.getWindowHandle();

					Set<String> oldWindows = driver.getWindowHandles();
					Set<String> newWindows = driver.getWindowHandles();

					for (String windowHandle : newWindows) {
						if (!oldWindows.contains(windowHandle)) {
							driver.switchTo().window(windowHandle);
							System.out.println("Closing unexpected new tab: " + driver.getTitle());
							driver.close(); // Close the new tab

							// Switch back to the original tab
							driver.switchTo().window(originalWindow);
							break;
						}
					}

				} catch (Exception e) {

				}
                results(); // Assuming you have a method named results()
                isPageLoaded = true;
                break; // Exit the loop if the element is displayed
            } catch (Exception e) {
                System.out.println("Wego SRP Page not Displayed - Retry #" + retryCount);
                Thread.sleep(50000);
                driver.get(Wego_URL);
            }
        }

        if (!isPageLoaded) {
            System.out.println("Wego SRP Page could not be loaded after " + maxRetries + " retries");
        }
    }
	
	public void results() throws InterruptedException {
	    List<String> allowedAirlines = Arrays.asList("F3","XY");

	    try {
	        // Click Direct Flights
	        WebElement directFlights = driver.findElement(By.xpath("//*/text()[normalize-space(.)='Direct']/parent::*"));
	        directFlights.click();

	        // Expand all filter sections
	        List<String> filterXpaths = Arrays.asList(
	            "//h3[contains(text(),'Payment method')]",
	            "//h3[contains(text(),'Stops')]",
	            "//h3[contains(text(),'Booking Options')]",
	            "//h3[contains(text(),'Price')]",
	            "//h3[contains(text(),'Flight Experience')]",
	            "//h3[contains(text(),'Flight Times')]",
	            "//h3[contains(text(),'Airlines/Alliances')]"
	        );

	        for (String xpath : filterXpaths) {
	            try {
	                WebElement filter = driver.findElement(By.xpath(xpath));
	                filter.click();
	                Thread.sleep(500);
	            } catch (Exception e) {
	                System.out.println("Could not expand filter: " + xpath);
	            }
	        }

	        // Extract departure date
	        WebElement Date = driver.findElement(By.xpath("//input[@data-testid='from-input-value']"));
	        String DepDate = Date.getAttribute("value").replaceAll("[\r\n]+", " ")
	                .replaceAll("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), ", "");
	        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
	        LocalDate parsedDate = LocalDate.parse(DepDate, inputFormatter);
	        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00");
	        String DepartDate = parsedDate.format(outputFormatter);

	        // Wait until results appear
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	        WebElement ResultsCount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),' of ')]")));

	        try {
	            WebElement ShowMore = driver.findElement(By.xpath("//div[contains(text(),'Show more')]"));
	            ShowMore.click();
	        } catch (Exception ignored) {}

	        String Results_Count = ResultsCount.getText();
	        String NumberResults = Results_Count.split(" ")[0];
	        if ("0".equals(NumberResults)) {
	            System.out.println("No Flights Available");
	            return;
	        }

	        Thread.sleep(1000);

	        WebElement From = driver.findElement(By.cssSelector("div[data-pw='leg_departureAirportCode']"));
	        WebElement To = driver.findElement(By.cssSelector("div[data-pw='leg_arrivalAirportCode']"));
	        String FromCity = From.getText();
	        String ToCity = To.getText();
	        String CurrencyCode = driver.findElement(By.cssSelector("button[aria-label='Currency']")).getText();
	        String TripType = "OW"; // Assuming one-way
	        String Client = "WEGO";
	        String Domain = "SA";

	        // Map<flightNumber, WegoFlightData>
	        Map<String, WegoFlightData> flightsDataMap = new HashMap<>();

	        // Collect Wego flights dynamically
	        List<WebElement> tripCards = driver.findElements(By.xpath("//div[@data-testid='trip-card']"));
	        
	        int Flightcount = 0;
            for (WebElement tripCard : tripCards) {
                String tripId = tripCard.getAttribute("data-trip-id");
                String flightNumber = null;
                if (tripId != null && tripId.contains(":")) {
                    String flightDetails = tripId.split(":")[1]; // Extract details after ':'
                    flightNumber = flightDetails.split("~")[0]; // Extract flight number
                    if (flightNumber != null && flightNumber.length() > 2) {
                    	Airline = flightNumber.substring(0, 2);
                    	if (!allowedAirlines.contains(Airline)) {
                            continue; // skip unallowed airlines
                        }
                        flightNumber = flightNumber.substring(2); // Remove the first two letters
                    } 
                }

                String websitesText = tripCard.findElement(By.xpath(".//span[@data-pw='tripCard_fareInfo']")).getText();
                if (websitesText.matches("from \\d+ websites")) {
                    // Extract other details
                    String airlineName = tripCard.findElement(By.xpath(".//span[@data-pw='leg_airlineName']")).getText();
                    String departureTime = tripCard.findElement(By.xpath(".//div[@data-testid='depart-time']")).getText();
                    String arrivalTime = tripCard.findElement(By.xpath(".//div[@data-testid='arrive-time']")).getText();
                    String departureAirport = tripCard.findElement(By.xpath(".//div[@data-pw='leg_departureAirportCode']")).getText();
                    String arrivalAirport = tripCard.findElement(By.xpath(".//div[@data-pw='leg_arrivalAirportCode']")).getText();
                    String price = tripCard.findElement(By.xpath(".//div[@data-pw='tripCard_price']")).getText().replace(",", "").replaceAll("[^0-9]", "").replace("SAR", "").replace("󡀁 ", "").trim();
                    int wegoPrice = Integer.parseInt(price);
                    // Format details into a single string
                    String flightDetails = Airline+""+flightNumber + " " + departureAirport + " " + departureTime + " " +
                            arrivalAirport + " " + arrivalTime + " " + price;
                    System.out.println(flightDetails);
                    Flightcount++;
                    // Add to the websites flight map
                    
                }
            }

            System.out.println("Total Flights: " + Flightcount);
	        
	        for (WebElement tripCard : tripCards) {
	            String tripId = tripCard.getAttribute("data-trip-id");
	            if (tripId == null || !tripId.contains(":")) continue;

	            String flightNumber = tripId.split(":")[1].split("~")[0];
	            String Airline = flightNumber.substring(0, 2);
	            if (!allowedAirlines.contains(Airline)) continue;

	            flightNumber = flightNumber.substring(2);

	            String priceText = tripCard.findElement(By.xpath(".//div[@data-pw='tripCard_price']"))
	                    .getText().replaceAll("[^0-9]", "").trim();
	            if (priceText.isEmpty()) continue;
	            int price = Integer.parseInt(priceText);

	            String departTime = tripCard.findElement(By.xpath(".//div[@data-testid='depart-time']")).getText();

	            flightsDataMap.put(flightNumber, new WegoFlightData(
	                    ToCity, FromCity, Airline, DepartDate, departTime,
	                    CurrencyCode, TripType, price, Client, "wego", Domain, flightNumber
	            ));
	        }

	        // Collect competitors dynamically
	        List<WebElement> competitorElements = driver.findElements(
	            By.xpath("//div[starts-with(@data-testid,'checkbox-')]//div[@class='jYR_Pv5_qCjd8DR_soPP']")
	        );
	        /*System.out.println("=== All competitors found on page ===");
	        for (WebElement el : competitorElements) {
	            System.out.println(el.getText().trim());
	        }*/

	        List<String> competitors = new ArrayList<>();
	        for (WebElement el : competitorElements) {
	            String name = el.getText().trim();
	            competitors.add(name);
	        }

	        Map<String, List<CompetitorFare>> competitorFares = new HashMap<>();
	        int domOrder = 1;
	        for (String compName : competitors) {
	            try {
	                WebElement checkbox = driver.findElement(
	                        By.xpath("//div[@data-testid='checkbox-" + compName + "']//div[@role='checkbox']"));
	                checkbox.click();
	                Thread.sleep(1000);

	                List<WebElement> compTrips = driver.findElements(By.xpath("//div[@data-testid='trip-card']"));
	                for (WebElement tripCard : compTrips) {
	                    String tripId = tripCard.getAttribute("data-trip-id");
	                    if (tripId == null || !tripId.contains(":")) continue;

	                    String flightNumber = tripId.split(":")[1].split("~")[0];
	                    String Airline = flightNumber.substring(0, 2);
	                    if (!allowedAirlines.contains(Airline)) continue;
	                    flightNumber = flightNumber.substring(2);

	                    String priceText = tripCard.findElement(By.xpath(".//div[@data-pw='tripCard_price']"))
	                            .getText().replaceAll("[^0-9]", "").trim();
	                    if (priceText.isEmpty()) continue;
	                    int price = Integer.parseInt(priceText);

	                    String departTime = tripCard.findElement(By.xpath(".//div[@data-testid='depart-time']")).getText();

	                    competitorFares.putIfAbsent(flightNumber, new ArrayList<>());
	                    competitorFares.get(flightNumber).add(new CompetitorFare(compName, price, departTime, Airline, domOrder));
	                }
	                domOrder++;

	                checkbox.click(); // uncheck
	                Thread.sleep(1000);
	            } catch (Exception ex) {
	                System.out.println("Skipping competitor: " + compName + " due to error.");
	            }
	        }

	     // 🔹 Build final WegoFlights JSON dynamically
	        for (String flightNumber : flightsDataMap.keySet()) {
	            WegoFlightData mainFlight = flightsDataMap.get(flightNumber);

	            List<CompetitorFare> fares = competitorFares.getOrDefault(flightNumber, new ArrayList<>());

	            // 🔹 Ensure Rehlat exists
	            boolean hasRehlat = fares.stream().anyMatch(f -> f.competitor.equalsIgnoreCase("rehlat"));
	            if (!hasRehlat) {
	                fares.add(new CompetitorFare("rehlat", 0, mainFlight.DepartTime, mainFlight.Airline, domOrder));
	            }

	            // 🔹 Sort strictly by price asc (Rehlat=0 treated as ∞ so fallback always last)
	            fares.sort((a, b) -> {
	                int priceA = (a.price == 0 && a.competitor.equalsIgnoreCase("rehlat")) ? Integer.MAX_VALUE : a.price;
	                int priceB = (b.price == 0 && b.competitor.equalsIgnoreCase("rehlat")) ? Integer.MAX_VALUE : b.price;
	                return Integer.compare(priceA, priceB);
	            });

	            // 🔹 Assign actual positions
	            int position = 1;
	            Map<CompetitorFare, Integer> assignedPositions = new LinkedHashMap<>();
	            for (CompetitorFare f : fares) {
	                int pos = (f.competitor.equalsIgnoreCase("rehlat") && f.price == 0) ? 0 : position++;
	                assignedPositions.put(f, pos);
	            }

	            // 🔹 Always include Rehlat
	            CompetitorFare rehlatFare = null;
	            int rehlatPos = 0;
	            for (Map.Entry<CompetitorFare, Integer> entry : assignedPositions.entrySet()) {
	                if (entry.getKey().competitor.equalsIgnoreCase("rehlat")) {
	                    rehlatFare = entry.getKey();
	                    rehlatPos = entry.getValue();
	                    break;
	                }
	            }

	            List<WegoFlights> flightsToSend = new ArrayList<>();
	            if (rehlatFare != null) {
	                flightsToSend.add(new WegoFlights(
	                        mainFlight.Destination, mainFlight.Source, rehlatFare.Airline,
	                        mainFlight.DepartDate, rehlatFare.DepartTime, mainFlight.CurrencyCode,
	                        mainFlight.TripType, rehlatFare.price, mainFlight.Client,
	                        rehlatFare.competitor.toLowerCase(), mainFlight.Domain,
	                        mainFlight.FltNum, String.valueOf(rehlatPos)
	                ));
	            }

	            // 🔹 Add 2 cheapest others (excluding Rehlat)
	            int added = 0;
	            for (Map.Entry<CompetitorFare, Integer> entry : assignedPositions.entrySet()) {
	                CompetitorFare f = entry.getKey();
	                int pos = entry.getValue();
	                if (f.competitor.equalsIgnoreCase("rehlat")) continue;
	                if (added >= 2) break;

	                flightsToSend.add(new WegoFlights(
	                        mainFlight.Destination, mainFlight.Source, f.Airline,
	                        mainFlight.DepartDate, f.DepartTime, mainFlight.CurrencyCode,
	                        mainFlight.TripType, f.price, mainFlight.Client,
	                        f.competitor.toLowerCase(), mainFlight.Domain,
	                        mainFlight.FltNum, String.valueOf(pos)
	                ));
	                added++;
	            }

	            // Debug
	            //System.out.println("=== Final API Payload (Rehlat + 2 cheapest others) ===");
	            for (WegoFlights wf : flightsToSend) {
	                System.out.println("Competitor: " + wf.Provider +
	                        " | Price: " + wf.APIPrice +
	                        " | Position: " + wf.Position);
	            }

	            // 🔹 Send to API
	            WegoApliClient.postCall(flightsToSend);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("No Direct Flights Available for this search");
	    }
	}

	class WegoFlightData {
	    String Destination, Source, Airline, DepartDate, DepartTime, CurrencyCode, TripType, Client, Domain, FltNum;
	    int APIPrice;

	    WegoFlightData(String dest, String src, String airline, String departDate, String departTime,
	                   String currency, String tripType, int price, String client, String provider, String domain, String fltNum) {
	        Destination = dest;
	        Source = src;
	        Airline = airline;
	        DepartDate = departDate;
	        DepartTime = departTime;
	        CurrencyCode = currency;
	        TripType = tripType;
	        APIPrice = price;
	        Client = client;
	        Domain = domain;
	        FltNum = fltNum;
	    }
	}

	class CompetitorFare {
	    String competitor, DepartTime, Airline;
	    int price, domOrder, position;

	    CompetitorFare(String competitor, int price, String departTime, String airline, int domOrder) {
	        this.competitor = competitor;
	        this.price = price;
	        this.DepartTime = departTime;
	        this.Airline = airline;
	        this.domOrder = domOrder;
	    }
	}



	public void Next_Dates() throws InterruptedException {
		
		for (int i = 1; i < 10; i++) {
		    try {
		        // Click the "from-chevron-right" element
		        
		        driver.findElement(By.xpath("//div[@data-testid='from-chevron-right']")).click();
		        Thread.sleep(1000);

		        // Click the "Search" button
		        driver.findElement(By.xpath("//button[@type='submit' and .//span[text()='Search']]")).click();
		        Thread.sleep(2000);
		        Wego_URL = driver.getCurrentUrl();
		        // Call the method to process results
		        resultsForWego();
		        System.out.println("Loop " + (i + 1) + " completed successfully.");
		    } catch (Exception e) {
		        System.err.println("Error occurred in loop " + (i + 1) + ": " + e.getMessage());
		    }
		}
	
	}

	public void wego_Login() throws InterruptedException {
	try {
	driver.get("https://auth.wego.com/user-auth/v2/login?site_code=SA&wc=654fadfa-3d1f-4679-a3b0-cc49237e21f3&ws=015bf179-cef7-4a98-8f00-20e2a734d2c8&locale=en&app_type=WEB_APP");
	Thread.sleep(5000);
	WebElement btnContinueEmail = driver.findElement(By.xpath("//div[contains(text(),'Continue with Email')]"));
	btnContinueEmail.click();
	Thread.sleep(2000);
	WebElement loginemail = driver.findElement(By.xpath("//input[@id='inputField']"));
	loginemail.sendKeys("flyadealreservations@gmail.com");
	WebElement btnLoginPassword = driver.findElement(By.xpath("//div[contains(text(),'Login with Password')]"));
	btnLoginPassword.click();
	Thread.sleep(2000);
	WebElement loginpassword = driver.findElement(By.xpath("//input[@id='passwordInputField']"));
	loginpassword.sendKeys("Flyadeal@123");
	Thread.sleep(1000);
	WebElement loginbtn = driver.findElement(By.xpath("//div[text()='Login' and contains(@class, 'loginWithPassword__lpbutton')]"));
	Thread.sleep(2000);
	loginbtn.click();
	Thread.sleep(10000);
	System.out.println("Wego SignIn");
	}
	catch (Exception e) {
	}
  }
}