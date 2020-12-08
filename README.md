
<p align="center"><img src="https://github.com/akshat46/boreas/blob/master/assets/logo.png" width="500"></p>

A decentralized encrypted offline chatting application.

## Table of Content

- Functionalities
- Architecture Overview
- Models
- Database Controllers
- Events
- Authentication
- Online Messaging
- Offline Messaging
- Radio Messaging
- Encryption
- Notifications

## Functionalities

Following are the major functionalities of boreas: 

- Allowing users to chat online. 
- Sending messages offline over long distance by incorporating location in its alogrithm and Google's Nearby Connections API. 
- Sending messages over radio device(Digi XBee SX 900) which can have a line-of-sight range of up to 65 miles.
- With the exception of radio messaging, both offline and online messaging are end-to-end encrypted with padded RSA cryptosystem.
- Sending media messages over online connection.
- Ability to scan nearby area for other users and chat with them regardless of whether they are in contacts list or not. The nearby area can be extended as far as 100ft.

## Architecture Overview

<p align="center"><img src="https://github.com/akshat46/boreas/blob/master/assets/architecture.svg" width="500"></p>
