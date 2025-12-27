package com.wegoflyadeal.helpers;

public class WegoFlights {

	public String Destination;
	public String Source;
	public String Airline;
	public String DepartDate;
	public String DepartTime;
	public String CurrencyCode;
	public String TripType;
	public int APIPrice;
	public String Client;
	public String Provider;
	public String Domain;
	public String FltNum;
	public int Position;


	public WegoFlights()
	{
	}

	public WegoFlights( String Destination, String Source, String Airline, String DepartDate, String DepartTime, String CurrencyCode, String TripType, int APIPrice, String Client,  String Provider,String Domain,String FltNum, String Position)
	{
		
		this.Destination = Destination;
		this.Source = Source;
		this.Airline = Airline;
		this.DepartDate = DepartDate;
		this.DepartTime = DepartTime;
		this.CurrencyCode = CurrencyCode;
		this.TripType = TripType;
		this.APIPrice = APIPrice;
		this.Client = Client;
		this.Provider = Provider;
		this.Domain = Domain;
		this.FltNum = FltNum;
		this.Position = (int)Integer.valueOf(Position);
	}
}
