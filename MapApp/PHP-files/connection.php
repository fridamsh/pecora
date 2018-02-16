<?php

define('hostname', 'phpmyadmin3.c2wxg7ustixm.eu-west-2.rds.amazonaws.com');
define('user', 'phpMyAdmin3');
define('password', 'phpMyAdmin3');
define('db_name', 'pecora');
$connect = mysqli_connect(hostname, user, password, db_name);

?>