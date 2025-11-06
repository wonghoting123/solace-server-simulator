# ACP Message 2658 Parsing Results

## Message Type: BCS-RT Request Account Info (Request)
**Message Code:** 2658 (BCS → ACP)

---

## Binary Message Overview

**Total Length:** 263 bytes (56-byte header + 206-byte body + 1-byte checksum)  
**Hex:** `620A16142400010100000000000000000000BC2E000000000000000000000000004F070000C60041413430303239386CE80E00000000000444440F0033080000014E414D4500000000000000000000000000000000004D454E4100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010701030000813F0100010100000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000057`

---

## Parsed Header (Bytes 1-56)

**Note:** For Source system number = 22/31 (BCS), the header includes 3 additional packet-related fields before the Reply code field.

| Field Name              | Byte Position | Hex Value          | Value (Decimal) | Notes                           |
|-------------------------|---------------|--------------------|------------------|---------------------------------|
| Message code            | 1-2           | 0x620A             | 2658            | BCS-RT Request Account Info      |
| Source system number    | 3             | 0x16               | 22              | From BCS                         |
| Destination system      | 4             | 0x14               | 20              | To ACP                           |
| **Packet group ID**     | **5-6**       | **0x2400**         | **36**          | **Packet group identifier (LE)** |
| **Packet sequence**     | **7**         | **0x01**           | **1**           | **This is packet 1**             |
| **Packet total**        | **8**         | **0x01**           | **1**           | **Total 1 packet in group**      |
| Reply code              | 9-10          | 0x0000             | 0               | N/A (request message)            |
| Last transaction ID     | 11-18         | 0x0000000000000000 | 0               | No previous transaction          |
| Message transaction ID  | 19-26         | 0x00000000BC2E0000 | 11,964          | Current transaction (LE: 0x2EBC) |
| Date                    | 27-30         | 0x00000000         | 0               | Not set                          |
| Time                    | 31-33         | 0x000000           | 0               | Not set                          |
| Location ID             | 34-37         | 0x0000004F         | 79              | Location 79 (LE)                 |
| Position no.            | 38-39         | 0x0700             | 7               | Position 7 (LE)                  |
| Physical terminal ID    | 40-47         | 0x00C6004141343030 | "AA4000"        | Terminal "AA4000" (with padding) |
| Staff ID                | 48-51         | 0x32393800         | "298" + null    | Staff ID "298" (ASCII)           |
| Logical terminal ID     | 52-55         | 0x6CE80E00         | 977,004         | Logical terminal (LE)            |
| Terminal type           | 56            | 0x00               | 0               | Terminal type 0                  |

---

## Parsed Body (Bytes 57-262)

### Standard Request Fields (Bytes 57-64)

| Document Position | Real Position | Field           | Hex Value    | Value (Decimal) | Notes                                    |
|-------------------|---------------|-----------------|--------------|-----------------|------------------------------------------|
| 53                | 57-60         | A/c number      | 0x00000004   | 4               | Account number (little-endian)           |
| 57                | 61-64         | Recorder track  | 0x44440F00   | 1,000,772       | Recorder: 10,007, Track: 72 (LE)         |

### Extended Account Opening Fields (Bytes 65-262)

| Document Position | Real Position | Field                      | Hex Value                                                                                      | Decoded Value           | Notes                           |
|-------------------|---------------|----------------------------|------------------------------------------------------------------------------------------------|-------------------------|---------------------------------|
| 61                | 65            | Customer salutation        | 0x33                                                                                           | '3' (51 ASCII)          | Salutation code                 |
| 62                | 66-86         | Customer surname           | 0x080000014E414D4500000000000000000000000000                                                   | "\x08\x00\x00\x01NAME" + nulls | Surname (21 bytes, null-padded) |
| 83                | 87-127        | Customer other name        | 0x4D454E410000000000000000000000000000000000000000000000000000000000000000000000000000         | "MENA" + nulls          | Other name (41 bytes, null-padded) |
| 124               | 128-139       | Customer Chinese surname   | 0x000000000000000000000000                                                                     | Empty (12 bytes)        | Chinese surname (null-padded)   |
| 136               | 140-151       | Customer Chinese other name| 0x000000000000000000000000                                                                     | Empty (12 bytes)        | Chinese other name (null-padded)|
| 148               | 152           | Channel Accessibility      | 0x01                                                                                           | 1                       | Channel access flag             |
| 149               | 153           | Ticket type                | 0x07                                                                                           | 7                       | Ticket type indicator           |
| 150               | 154           | Football type              | 0x01                                                                                           | 1                       | Football type indicator         |
| 151               | 155           | Spoken language            | 0x03                                                                                           | 3                       | Language preference             |
| 152               | 156           | Special A/C                | 0x00                                                                                           | 0                       | Special account flag            |
| 153               | 157           | Account type               | 0x00                                                                                           | 0                       | Account type (0 = normal)       |
| 154               | 158-161       | Security Code              | 0x813F0100                                                                                     | Various                 | Security code (4 bytes)         |
| 158               | 162           | Flag                       | 0x01                                                                                           | 1 (true)                | Boolean flag                    |
| 159               | 163           | Bank sequence - 1          | 0x01                                                                                           | 1                       | First bank sequence             |
| 160               | 164-167       | Bank number - 1            | 0x00000000                                                                                     | 0                       | First bank number (4 bytes)     |
| 164               | 168-171       | Branch number - 1          | 0x00000000                                                                                     | 0                       | First branch number (4 bytes)   |
| 168               | 172-184       | Bank account number - 1    | 0x0000000000000000000000000000                                                                 | Empty (13 bytes)        | First bank account (null-padded)|
| 181               | 185           | Bank sequence - 2          | 0x02                                                                                           | 2                       | Second bank sequence            |
| 182               | 186-189       | Bank number - 2            | 0x00000000                                                                                     | 0                       | Second bank number (4 bytes)    |
| 186               | 190-193       | Branch number - 2          | 0x00000000                                                                                     | 0                       | Second branch number (4 bytes)  |
| 190               | 194-206       | Bank account number - 2    | 0x0000000000000000000000000000                                                                 | Empty (13 bytes)        | Second bank account (null-padded)|
| 203               | 207           | Restricted                 | 0x00                                                                                           | 0                       | Restricted flag                 |
| 204               | 208-220       | Online application ref no. | 0x0000000000000000000000000000                                                                 | Empty (13 bytes)        | Application reference number    |
| 217               | 221           | eWallet Only Indicator     | 0x00                                                                                           | 0                       | eWallet only flag               |
| 218               | 222-262       | CRM reference no.          | 0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000       | Empty (41 bytes)        | CRM reference (null-padded)     |

### Checksum (Byte 263)

| Field     | Byte Position | Hex Value | Value (Decimal) | Notes           |
|-----------|---------------|-----------|-----------------|-----------------|
| Checksum  | 263           | 0x57      | 87              | Message checksum|
