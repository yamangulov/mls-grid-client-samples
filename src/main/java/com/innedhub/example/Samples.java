package com.innedhub.example;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.innedhub.MLSGridClient;
import com.innedhub.MLSGridFactory;
import com.innedhub.enums.MLSResource;
import com.innedhub.results.PropertyTO;
import com.innedhub.results.SearchResult;

import java.net.URI;
import java.util.List;

public class Samples {
    private static String bucketName = "";
    private static String region = "";
    private static String awsAccessKey = "";
    private static String awsSecretKey = "";
    private static String yourApiUri = "https://api.mlsgrid.com/";
    private static String yourApiKey = "";

    public static void main(String[] args) {
        //initialization factory and client
        MLSGridFactory factory = new MLSGridFactory();
        MLSGridClient gridClient = factory.createClient(yourApiUri, yourApiKey);
        //general format of search, variants of search filters syntax are like as https://docs.mlsgrid.com/#mls-grid-service--api or https://docs.microsoft.com/en-us/azure/search/search-query-odata-filter
        SearchResult searchResult = gridClient.searchResult(MLSResource.PROPERTY_RESI, "ListingId eq 'MRD06341151' or ListingId eq 'MRD06340449' and MlgCanView eq true");
        //by default searchResult() returns 5000 records per request https://docs.mlsgrid.com/#limitations-of-replication-api , so in order to get next pages of request, use pagination with "nextPage" field
        SearchResult searchResult2 = gridClient.searchResult(MLSResource.PROPERTY_RESI, "ModificationTimestamp gt 2020-02-04T23:59:59.99Z");
        //property list from search result
        List<PropertyTO> propertyTOList = searchResult2.getPropertyTOList();
        //next page for search result
        URI nextPage = searchResult2.nextPage();
        //checking if next page exists
        boolean isNextPageExists = searchResult2.hasNextPage();
        //to get result form next page with using URI
        SearchResult resultFromNextPage = gridClient.searchResult(nextPage);
        //or in the loop
        while(searchResult2.hasNextPage()) {
            searchResult2 = gridClient.searchResult(nextPage);
            //your operations with the current search result
        }
        //also search request can be limited, i.e 250 top returning objects
        SearchResult searchResult3 = gridClient.searchResult(MLSResource.PROPERTY_RESI, "ModificationTimestamp gt 2020-02-04T23:59:59.99Z", 250);

        //getting and saving images from api.mlsgrid.com to AWS S3 storage
        //at first you need to initialize connection to your AWS S3 storage
        gridClient.initAmazonConnection(bucketName, region, awsAccessKey, awsSecretKey, gridClient);
        //saving images by mlsnumber
        gridClient.getAndSaveAllImages("MRD10611226");
        //saving images by mlsnumber with top limit of their quantity
        gridClient.getAndSaveAllImages("MRD10611226", 4);
        //if you need to delete certain image by its aws s3 object id
        AmazonS3 amazonS3 = gridClient.getAmazonS3();
        amazonS3.deleteObject(bucketName, "thumbnail_MRD10611226.jpg");
        //browsing objects in bucket
        S3Objects.inBucket(amazonS3, bucketName).forEach((S3ObjectSummary objectSummary) -> {
            System.out.println(objectSummary.getKey());
        });

    }
}
