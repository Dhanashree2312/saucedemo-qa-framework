package com.qa.tests.ui;

import com.qa.framework.config.ConfigManager;
import com.qa.framework.pages.InventoryPage;
import com.qa.framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class InventoryTest extends BaseTest {
    private InventoryPage inventoryPage;

    @DataProvider(name="sortingOptions")
    public Object[][] sortingOptions(){
        return new Object[][]{
                {"az","name",false},
                {"za","name",true},
                {"lohi","price",false},
                {"hilo","price",true}
        };
    }

    @BeforeMethod
    public void setUpInventoryPage(){
        String username = ConfigManager.getProperty("inventory_username");
        String password = ConfigManager.getProperty("inventory_password");

        // Open saucelab website
        page.navigate(ConfigManager.getProperty("base_url"));

        //Create loginpage object
        LoginPage loginpage = new LoginPage(page);

        //Login to the website using inventory standard user
        loginpage.login(username,password);

        //Check if login is successful
        Assert.assertTrue(page.url().contains("/inventory.html"),
                "User is successfully logged in to SauceLab.");

        //Check if user can see list of products and its prices successfully.
        inventoryPage = new InventoryPage(page);
        inventoryPage.checkInventoryPage();
    }

    @Test
    public void inventoryTest(){

        List<String> productNames=inventoryPage.getProductNames();
        List<String> productPrices=inventoryPage.getProductPrices();

        Assert.assertFalse(productNames.isEmpty(),"Product names should not be empty.");
        Assert.assertFalse(productPrices.isEmpty(),"Product prices should not be empty");
        Assert.assertEquals(productNames.size(),productPrices.size(),"Every product must have corresponding price for it.");

        //Verify product names should be unique
        Set<String> uniqueNames = new HashSet<>(productNames);
        Assert.assertEquals(uniqueNames.size(),productNames.size(),"" +
                "Duplicate product names are found");

        //Verify all product names are populated
        Assert.assertTrue(productNames.stream().allMatch(name->name!=null && !name.isBlank()),
                "every product should have a name");

        //Verify if every price follows a standard pattern
        Pattern priceMatcher = Pattern.compile("\\$\\d+\\.\\d{2}");
        Assert.assertTrue(productPrices.stream().allMatch(
                price->price!=null ||
                        priceMatcher.matcher(price.trim()).matches()),
                "Every price should follow standard pattern format of $0.00"
        );
    }

    @Test(dataProvider = "sortingOptions")
    public void verifyProductSorting
            (String option, String sortType, boolean descending){
        inventoryPage.sortProductsByNameAndPrice(option);
        if(sortType.equals("name")){
            List<String> actualProductName = inventoryPage.getProductNames();
            List<String> expectedProductName = new ArrayList<>(actualProductName);
            expectedProductName.sort(String.CASE_INSENSITIVE_ORDER);
            if(descending){
                Collections.reverse(expectedProductName);
            }
            Assert.assertEquals(actualProductName,expectedProductName,"" +
                    "Product name sorting failed for option"+option+" and sorting type"+sortType);
        }
        else if(sortType.equals("price")){
            List<String> actualPrices = inventoryPage.getProductPrices();
            List<BigDecimal> actual = actualPrices.stream().map(
                    price-> new BigDecimal(price.replace("$","").trim()))
                    .toList();
            List<BigDecimal> expected = new ArrayList<>(actual);
            Collections.sort(expected);
            if(descending){
                Collections.reverse(expected);
            }
            Assert.assertEquals(
                    actual,
                    expected,
                    "Product price sorting failed for option: " + option +"and sort type"+sortType
            );}
            else{
            Assert.fail("Unsupported sorting type: " + sortType);
            }
        }
    }

