import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GenerateHandoverPdf {

    static class PageContent {
        StringBuilder stream = new StringBuilder();
        int y = 790;
        int pageNum;

        PageContent(int pageNum) {
            this.pageNum = pageNum;
            // Header
            stream.append("BT /F2 8 Tf 50 810 Td (REAL-TIME TRAIN TRACKER - SYSTEM SUMMARY & HANDOVER REPORT) Tj ET\n");
            stream.append("0.5 0.5 0.5 RG 0.5 w 50 805 m 545 805 l S 0 0 0 RG\n");
            // Footer
            stream.append("0.5 0.5 0.5 RG 0.5 w 50 40 m 545 40 l S 0 0 0 RG\n");
            stream.append("BT /F1 8 Tf 50 30 Td (Generated for AI Handover & Next Development Steps | Page " + pageNum + ") Tj ET\n");
        }

        void addTitle(String text) {
            y -= 12;
            stream.append("BT /F2 16 Tf 0.1 0.2 0.4 rg 50 ").append(y).append(" Td (").append(escape(text)).append(") Tj ET 0 0 0 rg\n");
            y -= 22;
        }

        void addHeading1(String text) {
            y -= 14;
            stream.append("BT /F2 12 Tf 0.15 0.3 0.6 rg 50 ").append(y).append(" Td (").append(escape(text)).append(") Tj ET 0 0 0 rg\n");
            y -= 16;
        }

        void addHeading2(String text) {
            y -= 8;
            stream.append("BT /F2 10 Tf 0.2 0.2 0.2 rg 50 ").append(y).append(" Td (").append(escape(text)).append(") Tj ET 0 0 0 rg\n");
            y -= 14;
        }

        void addParagraph(String text) {
            List<String> lines = wrap(text, 92);
            for (String line : lines) {
                stream.append("BT /F1 9 Tf 50 ").append(y).append(" Td (").append(escape(line)).append(") Tj ET\n");
                y -= 12;
            }
            y -= 4;
        }

        void addBullet(String boldPrefix, String text) {
            List<String> lines = wrap(boldPrefix + " " + text, 88);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0) {
                    stream.append("BT /F2 9 Tf 50 ").append(y).append(" Td (\\225 ) Tj ET\n");
                    stream.append("BT /F1 9 Tf 62 ").append(y).append(" Td (").append(escape(line)).append(") Tj ET\n");
                } else {
                    stream.append("BT /F1 9 Tf 62 ").append(y).append(" Td (").append(escape(line)).append(") Tj ET\n");
                }
                y -= 12;
            }
            y -= 2;
        }

        void addCodeBlock(List<String> codeLines) {
            int blockHeight = (codeLines.size() * 11) + 10;
            int boxY = y - blockHeight + 10;
            // Background box
            stream.append("0.95 0.95 0.95 rg 50 ").append(boxY).append(" 495 ").append(blockHeight).append(" re f\n");
            stream.append("0.8 0.8 0.8 RG 0.5 w 50 ").append(boxY).append(" 495 ").append(blockHeight).append(" re S 0 0 0 RG 0 0 0 rg\n");
            int textY = y;
            for (String cl : codeLines) {
                stream.append("BT /F3 8 Tf 58 ").append(textY).append(" Td (").append(escape(cl)).append(") Tj ET\n");
                textY -= 11;
            }
            y -= (blockHeight + 8);
        }

        static String escape(String s) {
            return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        }

        static List<String> wrap(String s, int maxChars) {
            List<String> res = new ArrayList<>();
            String[] words = s.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (sb.length() + w.length() + 1 > maxChars) {
                    res.add(sb.toString());
                    sb = new StringBuilder();
                }
                if (sb.length() > 0) sb.append(" ");
                sb.append(w);
            }
            if (sb.length() > 0) res.add(sb.toString());
            return res;
        }
    }

    public static void main(String[] args) throws Exception {
        List<PageContent> pages = new ArrayList<>();

        // ══════════════════════════════════════════════════════════
        // PAGE 1: Overview, Tech Stack & Environment Setup
        // ══════════════════════════════════════════════════════════
        PageContent p1 = new PageContent(1);
        p1.addTitle("Real-Time Train Movement Tracker - Handover Document");
        p1.addParagraph("This document provides a complete technical briefing, architectural audit, file-by-file record of modifications, and an actionable roadmap for AI agents or software engineers continuing development on this system.");

        p1.addHeading1("1. System Overview & Core Capabilities");
        p1.addParagraph("The application is a full-stack railway geospatial tracking and telemetry visualization system based on the 'Visual Representation of Real Time Train Movement' SRS specification.");
        p1.addBullet("Search & Schedule:", "Users can query any Indian Railways train by number (e.g., 12002, 12952) or name.");
        p1.addBullet("Real-Time Telemetry:", "Consumes live position, speed, delay, bearing, and next-halt info from external APIs.");
        p1.addBullet("Track Geometry Snapping:", "Snaps and interpolates raw coordinates directly onto physical railway GeoJSON tracks.");
        p1.addBullet("Live WebSocket Delivery:", "Pushes telemetry over persistent STOMP/SockJS WebSockets without browser refreshing.");
        p1.addBullet("Physical Railway Layer:", "Integrates OpenRailwayMap into Leaflet to render actual tracks, junctions, and switches.");

        p1.addHeading1("2. Environment & Toolchain Setup");
        p1.addParagraph("During the deployment process, the local development environment was upgraded to modern standards:");
        p1.addBullet("Java Environment:", "System previously had Java 8 (JRE 1.8.0_503). Configured to use JDK 27 located at C:\\jdk-27.0.1.1 (Java 27.0.1.1 64-bit), fully compatible with Spring Boot 3 Java 17+ requirement.");
        p1.addBullet("Build Tool:", "Downloaded and configured Apache Maven 3.9.9 locally into .tools\\apache-maven.");
        p1.addBullet("Execution Scripts:", "Created run.bat and run.ps1 scripts that automatically configure JAVA_HOME and M2_HOME paths.");

        p1.addHeading1("3. External API Verification (RailRadar)");
        p1.addParagraph("The user provided a live RailRadar developer API key: rg_92e533fd40574b779cfe224dc2b760b5.");
        p1.addParagraph("This key was verified against live Indian Railways servers:");
        p1.addBullet("Live Telemetry:", "GET https://api.railradar.in/v1/trains/12002/live (Shatabdi Express) - HTTP 200 OK.");
        p1.addBullet("Live Night Express:", "GET https://api.railradar.in/v1/trains/12952/live (Tejas Rajdhani) - HTTP 200 OK (357.8 km progress).");
        p1.addBullet("Route Geometry:", "GET https://api.railradar.in/v1/trains/12002/route?format=geojson - HTTP 200 OK GeoJSON LineString.");
        pages.add(p1);

        // ══════════════════════════════════════════════════════════
        // PAGE 2: Codebase Modifications & Implementations
        // ══════════════════════════════════════════════════════════
        PageContent p2 = new PageContent(2);
        p2.addTitle("Modifications & Technical Enhancements");

        p2.addHeading1("4. Detailed File-by-File Changes");

        p2.addHeading2("A. LiveDataProviderService.java");
        p2.addParagraph("Modernized the provider service to support the official RailRadar v1 REST specification:");
        p2.addBullet("Dynamic URLs:", "Configured endpoints for /trains/{trainNumber}/live and /trains/{trainNumber}/route?format=geojson.");
        p2.addBullet("Telemetry Parsing:", "Handles nested data.currentLocation objects (lat, lng, speedKmh, bearingDegrees, segmentProgress, delayMinutes).");
        p2.addBullet("Progress Fallback:", "If direct coordinates are omitted, computes segmentProgress = distanceFromOriginKm / totalDistance.");
        p2.addBullet("GeoJSON Extraction:", "Unwraps data.geojson Feature objects directly for clean Leaflet consumption.");

        p2.addHeading2("B. PositionProcessingService.java");
        p2.addParagraph("Upgraded spatial processing algorithms to handle complex GeoJSON geometries:");
        p2.addBullet("Multi-format Coords:", "Extracts coordinate arrays from both raw LineStrings and GeoJSON Features with geometry.coordinates.");
        p2.addBullet("Full-Route Interpolation:", "Added interpolateAlongEntireRoute(geoJson, fraction) using Haversine segment calculations to place trains along the exact railway track based on route distance progress.");
        p2.addBullet("Route Snapping:", "Added snapToRoute() to constrain GPS drift within a 500-meter tolerance.");

        p2.addHeading2("C. Database Architecture & H2 Embedded Profile");
        p2.addParagraph("To eliminate MySQL dependency blocks for immediate deployment, an embedded in-memory H2 profile was built:");
        p2.addBullet("pom.xml:", "Added com.h2database:h2 runtime dependency alongside MySQL driver.");
        p2.addBullet("application-h2.properties:", "Configured H2 in-memory URL (jdbc:h2:mem:train_tracker), Hibernate H2 dialect, and Jackson configuration.");
        p2.addBullet("DatabaseSeeder.java:", "Created a clean Spring CommandLineRunner that seeds reference stations (NDLS, BCT, AGC, BPL, etc.), trains, and GeoJSON routes programmatically via JPA repositories, avoiding raw SQL syntax errors.");
        pages.add(p2);

        // ══════════════════════════════════════════════════════════
        // PAGE 3: Frontend & Next Steps Roadmap
        // ══════════════════════════════════════════════════════════
        PageContent p3 = new PageContent(3);
        p3.addTitle("Frontend Upgrades & Handover Action Plan");

        p3.addHeading1("D. Leaflet Frontend UI & Railway Track Layer");
        p3.addParagraph("Enhanced the browser frontend (src/main/resources/static/):");
        p3.addBullet("OpenRailwayMap Tile Layer:", "Added https://{s}.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png overlay, rendering real physical railway lines, signals, switches, and tracks beneath the train marker.");
        p3.addBullet("Layer Control Switcher:", "Users can toggle between 'Dark Railway View', 'Standard Map', and 'Railway Tracks (OpenRailwayMap)'.");
        p3.addBullet("Marker & Animation:", "Added animated CSS radar pulse beacon (.train-beacon-pulse), bearing rotation, and station halt popups.");
        p3.addBullet("Dynamic On-Demand Import:", "When searching any 5-digit train number, the backend queries RailRadar, persists the route geometry, and loads it into Leaflet.");

        p3.addHeading1("5. How to Run the Project");
        p3.addParagraph("The application can be started using the pre-configured scripts:");
        List<String> runCode = Arrays.asList(
            "# Option 1: Run the startup batch file",
            ".\\run.bat",
            "",
            "# Option 2: Run via standalone packaged JAR using Java 27",
            "& 'C:\\jdk-27.0.1.1\\bin\\java.exe' -jar target\\train-tracker-1.0.0.jar --spring.profiles.active=h2",
            "",
            "# Open in browser: http://localhost:8080"
        );
        p3.addCodeBlock(runCode);

        p3.addHeading1("6. Action Plan for Next AI / Developer");
        p3.addBullet("Task 1 - Multi-Train Tracking:", "Currently, tracking focuses on one train at a time. Enhance TrackingService and app.js to support multi-train tracking on a single map view simultaneously.");
        p3.addBullet("Task 2 - ETA & Delay Timeline:", "Render intermediate station timeline in the sidebar with live arrival countdowns and delayed flags.");
        p3.addBullet("Task 3 - MySQL Production Profile:", "To switch from H2 to MySQL, start MySQL service on 3306, create train_tracker DB using sql/schema.sql, and launch without the --spring.profiles.active=h2 flag.");
        p3.addBullet("Task 4 - Production Deployment:", "Create a Dockerfile with openjdk:17-slim or eclipse-temurin:17 for one-click cloud hosting (Render, AWS ECS, or DigitalOcean).");

        pages.add(p3);

        // ══════════════════════════════════════════════════════════
        // BUILD PDF BINARY STRUCTURE
        // ══════════════════════════════════════════════════════════
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Long> offsets = new ArrayList<>();
        offsets.add(0L); // 0th dummy

        baos.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

        int totalPages = pages.size();
        // Object IDs:
        // 1: Catalog
        // 2: Pages
        // 3: Font Helvetica
        // 4: Font Helvetica-Bold
        // 5: Font Courier
        // Page 1: 6 (Page), 7 (Content)
        // Page 2: 8 (Page), 9 (Content)
        // Page 3: 10 (Page), 11 (Content)

        int font1Id = 3;
        int font2Id = 4;
        int font3Id = 5;

        // 1 0 obj Catalog
        offsets.add((long) baos.size());
        baos.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 2 0 obj Pages
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < totalPages; i++) {
            kids.append((6 + i * 2)).append(" 0 R ");
        }
        offsets.add((long) baos.size());
        baos.write(("2 0 obj\n<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + totalPages + " >>\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));

        // 3 0 obj Font Helvetica
        offsets.add((long) baos.size());
        baos.write("3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 4 0 obj Font Helvetica-Bold
        offsets.add((long) baos.size());
        baos.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // 5 0 obj Font Courier
        offsets.add((long) baos.size());
        baos.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

        // Pages and Contents
        for (int i = 0; i < totalPages; i++) {
            int pageId = 6 + i * 2;
            int contentId = 7 + i * 2;
            PageContent pc = pages.get(i);
            byte[] contentBytes = pc.stream.toString().getBytes(StandardCharsets.ISO_8859_1);

            // Page obj
            offsets.add((long) baos.size());
            String pageObj = pageId + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                    "/Resources << /Font << /F1 3 0 R /F2 4 0 R /F3 5 0 R >> >> " +
                    "/Contents " + contentId + " 0 R >>\nendobj\n";
            baos.write(pageObj.getBytes(StandardCharsets.ISO_8859_1));

            // Content obj
            offsets.add((long) baos.size());
            String contentObjHeader = contentId + " 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n";
            baos.write(contentObjHeader.getBytes(StandardCharsets.ISO_8859_1));
            baos.write(contentBytes);
            baos.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
        }

        // XRef
        int totalObjects = 5 + totalPages * 2;
        long startXref = baos.size();
        baos.write(("xref\n0 " + (totalObjects + 1) + "\n0000000000 65535 f \n").getBytes(StandardCharsets.ISO_8859_1));
        for (int i = 1; i <= totalObjects; i++) {
            baos.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
        }

        // Trailer
        baos.write(("trailer\n<< /Size " + (totalObjects + 1) + " /Root 1 0 R >>\nstartxref\n" + startXref + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

        // Save file
        String outputPath = "PROJECT_SUMMARY_AND_HANDOVER.pdf";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(baos.toByteArray());
        }
        System.out.println("Handover PDF successfully written to " + outputPath + " (" + baos.size() + " bytes)");
    }
}
