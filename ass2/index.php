

<!-- popcorn3.php - Processes the form described in
     popcorn3.html
     -->
<html>
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

//read data from order.txt 
      $filename = 'order.txt';
      // if file_exists($filename)
      //   $file = fopen($filename, 'r');
      // else
      //   exit("Unable to open file ($filename)");

      $fruits = file($filename);
      //fclose($filename);

//get current amount of each fruit and calculate the new amount
      $appleString = preg_split("/[\s,]+/", $fruits[0]);
      $oldApple = $appleString[4];
      $newApple = $oldApple + $apple;

      $orangeString = preg_split("/[\s,]+/", $fruits[1]);
      $oldOrange = $orangeString[4];
      $newOrange = $oldOrange + $orange;

      $bananaString = preg_split("/[\s,]+/", $fruits[2]);
      $oldBanana = $bananaString[4];
      $newBanana = $oldBanana + $banana;

//update the amount of fruits in the order.txt
      $file = fopen($filename, 'w') 
        or exit("Unable to open file ($filename)");

      fwrite($file, "Total number of apples: $newApple\n");
      fwrite($file, "Total number of apples: $newOrange\n");
      fwrite($file, "Total number of apples: $newBanana\n");

      fclose($filename);

// Return the results to the browser in a table

    ?>

    <table border = "border">
      <caption> Order Information </caption>
      <tr align = "center">
        <th colspan = "2">Customer: </th>
        <td colspan = "2"><?php print ("$name <br />"); ?></td>
      </tr>
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
      <tr align = "center">
        <th colspan = "2">Total Price: </th>
        <td colspan = "2"><?php printf("$ %5.2f",$total_price) ?></td>
      </tr>
      <tr align = "center">
        <th colspan = "2">Payment Method: </th>
        <td colspan = "2"><?php print($payment) ?></td>
      </tr>
    </table>
    <p /> <p />

  </body>
</html>

