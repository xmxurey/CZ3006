<?xml version = "1.0"  encoding = "utf-8" ?>
<!DOCTYPE html PUBLIC "-//w3c//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<!-- popcorn3.php - Processes the form described in
     popcorn3.html
     -->
<html xmlns = "http://www.w3.org/1999/xhtml">
  <head>
    <title> Process the index.html form </title>
  </head>
  <body>
    <?php

// Get form data values

      $apple = $_POST["apple"];
      $orange = $_POST["orange"];
      $banana = $_POST["banana"];
      $name = $_POST["name"];
      $payment = $_POST["payment"];

// Compute the item costs and total cost

      $apple_cost = 0.69 * $apple;
      $orange_cost = 0.59 * $orange;
      $banana_cost = 0.39 * $banana;
      $total_price = $apple_cost + $orange_cost + 
                     $banana_cost;
      $total_items = $apple + $orange + $banana;

// Return the results to the browser in a table

    ?>
    <h4> Customer: </h4>
    <?php
      print ("$name <br />");
    ?>
    <p /> <p />

    <table border = "border">
      <caption> Order Information </caption>
      <tr>
        <th> Product </th>
        <th> Unit Price </th>
        <th> Quantity Ordered </th>
        <th> Item Cost </th>
      </tr>
      <tr align = "center">
        <td> Apple </td>
        <td> $0.69 </td>
        <td> <?php print ("$apple"); ?> </td>
        <td> <?php printf ("$ %4.2f", $apple_cost); ?>
        </td>
      </tr>
      <tr align = "center">
        <td> Orange </td>
        <td> $0.59 </td>
        <td> <?php print ("$orange"); ?> </td>
        <td> <?php printf ("$ %4.2f", $orange_cost); ?>
        </td>
        </tr>
      <tr align = "center">
        <td> Banana </td>
        <td> $0.39 </td>
        <td> <?php print ("$banana"); ?> </td>
        <td> <?php printf ("$ %4.2f", $banana_cost); ?>
        </td>
      </tr>
    </table>
    <p /> <p />

    <?php
      print ("You ordered $total_items fruit items <br />");
      printf ("Your total bill is: $ %5.2f <br />", $total_price);
      print ("Your chosen method of payment is: $payment <br />");
    ?>
  </body>
</html>

