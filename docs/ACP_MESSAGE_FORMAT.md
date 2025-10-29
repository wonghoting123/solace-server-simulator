# ACP Message Format Documentation

## Overview

This document describes the Application Communication Protocol (ACP) message format used for communication between BCS (Betting Computer System) and ACP systems. All messages follow a common structure with a mandatory header and optional message-specific content.

## Message Structure

### Common Message Header

All ACP messages must contain the following header structure (52 bytes):

| Byte Position | Data Type | Size (bytes) | Field Name | Description |
|--------------|-----------|--------------|------------|-------------|
| 0-1 | Unsigned Integer | 2 | Message Code | Identifies the message type |
| 2 | Integer | 1 | Source System Number | System sending the message |
| 3 | Integer | 1 | Destination System Number | System receiving the message |
| 4-5 | Unsigned Integer | 2 | Reply Code | Status or error code in replies |
| 6-13 | Unsigned Integer | 8 | Last Transaction ID | Previous transaction ID (max 16GB) |
| 14-21 | Unsigned Integer | 8 | Message Transaction ID | Current transaction ID (max 16GB) |
| 22-25 | Unsigned Integer | 4 | Date | Transaction date |
| 26-28 | Unsigned Integer | 3 | Time | Transaction time |
| 29-32 | Unsigned Integer | 4 | Location ID | Physical location identifier |
| 33-34 | Unsigned Integer | 2 | Position Number | Terminal position |
| 35-42 | String | 8 | Physical Terminal ID | Terminal identifier |
| 43-46 | Unsigned Integer | 4 | Staff ID | Operator identifier |
| 47-50 | Unsigned Integer | 4 | Logical Terminal ID | Logical terminal identifier |
| 51 | Unsigned Integer | 1 | Terminal Type | Type of terminal |

**Mandatory Header Fields:**
- Message Code
- Source System Number
- Destination System Number
- Message Transaction ID

**Optional Message Content:**
- Starting at byte position 52
- Variable length depends on message type
- All bits initialized as zero

### Byte Order

**Important:** All multi-byte numeric fields use **little-endian** byte order.

Example:
- Hex bytes: `F3 0A` (at positions 0-1)
- Binary: `11110011 00001010`
- Little-endian value: `0x0AF3` = 2803 (decimal)

## Message Categories

### 1. Status Enquiry Messages

#### Status Enquiry for BCS Transactions
- **Request (BCS→ACP):** Message Code 2073
- **Reply (ACP→BCS):** Message Code 2074
- **Purpose:** Check transaction status after timeout
- **Additional Data (byte 52+):**
  - Current/last processed transaction ID (8 bytes, unsigned integer)

#### Status Enquiry for ACP Transactions
- **Request (ACP→BCS):** Message Code 3019
- **Reply (BCS→ACP):** Message Code 3020
- **Header Required:** Transaction ID, Logical Terminal ID
- **Additional Data (byte 52+):**
  - Current/last processed transaction ID (8 bytes, unsigned integer)

#### Implicit Reverse for ACP Transactions
- **Message Code:** 3022 (BCS→ACP)
- **Purpose:** Auto-reverse when status request confirms successful processing
- **Header Required:** Reply code, Logical Terminal ID

### 2. Account Management Messages (BCS-BG)

#### Account Bet Parameter
- **Request:** Message Code 2500
- **Reply:** Message Code 2501
- **Purpose:** Manage betting parameters for accounts

#### Total Account Balance Request
- **Request:** Message Code 2505
- **Reply:** Message Code 2506
- **Purpose:** Query account balance

### 3. Real-Time Account Messages (BCS-RT)

#### Account Open
- **Request:** Message Code 2604
- **Reply:** Message Code 2659

#### Account Close
- **Request:** Message Code 2606
- **Reply:** Message Code 2607

#### Change Security Code
- **Request:** Message Code 2608
- **Reply:** Message Code 2605

#### Debit/Credit Transaction
- **Request:** Message Code 2610
- **Reply:** Message Code 2611

#### Validate Bet
- **Request:** Message Code 2612
- **Reply:** Message Code 2613

#### Online Statement
- **Request:** Message Code 2650
- **Reply:** Message Code 2651

#### Extended Online Statement
- **Request:** Message Code 2652
- **Reply:** Message Code 2653

#### Account Details Enquiry
- **Request:** Message Code 2654
- **Reply:** Message Code 2655

#### Account Details Update
- **Request:** Message Code 2656
- **Reply:** Message Code 2660

And many more... (see full list in specification)

## Error Codes

### Common Reply Codes

| Code | Description |
|------|-------------|
| 0 | Success |
| 1 | Invalid message format |
| 2 | Transaction not received |
| 3 | Old transaction |
| 4 | Reverse cannot be done |

## Usage Example

### Parsing Hexadecimal Message

**Input Hex String:**
```
F30A291408000101000000000000000000002A5AB300000000001209E8070C0C375E07...
```

**Parsing Steps:**

1. **Bytes 0-1 (Message Code):**
   - Hex: `F3 0A`
   - Binary: `11110011 00001010`
   - Little-endian: `0x0AF3` = 2803 (decimal)
   - **Result:** Message Code is 2803

2. **Byte 2 (Source System):**
   - Hex: `29`
   - Decimal: 41

3. **Byte 3 (Destination System):**
   - Hex: `14`
   - Decimal: 20

## API Implementation

The `AcpMessageParser` service provides:
- Hex string to binary conversion
- Little-endian byte order handling
- Field extraction and mapping
- Input validation

### REST Endpoints

- `POST /api/acp/parse` - Parse hex string to ACP message fields
- `GET /api/acp/info` - Get parser information

## References

- Original specification: `acp_message.rtf`
- Implementation: `src/main/java/com/solace/simulator/service/AcpMessageParser.java`
- Models: `src/main/java/com/solace/simulator/model/`
