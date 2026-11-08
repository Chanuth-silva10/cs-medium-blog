# CS Spring Boot File Upload
Safe file uploads with Spring Boot 4 and Java 21: expense receipts (PDF, PNG, JPEG) sent as multipart/form-data, checked by their first bytes, stored under random UUID names and served back through a controller. Part of the DevCraft series by CS Solution (Chapter 23).
## Endpoints
| Method | Path | What it does |
| --- | --- | --- |
| POST | `/receipts` | Upload a receipt (parts: `file`, `employeeName`, `expenseDate`, `amount`) |
| GET | `/receipts` | List receipts, newest first |
| GET | `/receipts/{id}` | Details of one receipt |
| GET | `/receipts/{id}/file` | The stored file (`?download=true` forces a download) |
| DELETE | `/receipts/{id}` | Delete a receipt and its file |
## Run
```bash
mvn spring-boot:run
```
The API starts on `http://localhost:7500` and stores files in `./uploads/receipts` (change it with the `STORAGE_LOCATION` environment variable).
```bash
curl -F "file=@receipt.pdf" -F "employeeName=Nadia Perera" -F "expenseDate=2026-11-03" -F "amount=42.50" http://localhost:7500/receipts
```
## Settings
| Property | Default | Meaning |
| --- | --- | --- |
| `app.storage.location` | `./uploads/receipts` | Folder for stored files |
| `app.storage.serve-statically` | off | Maps `/public-uploads/**` onto the folder. Makes every file public, read the article first |
| `spring.servlet.multipart.max-file-size` | 5MB | Larger files get 413 |
## Test
```bash
mvn test
```
