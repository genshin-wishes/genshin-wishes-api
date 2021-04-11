package com.uf.genshinwishes.service;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.context.MessageSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CSVHelper {

    public static ByteArrayInputStream wishesToCsv(MessageSource messageSource, User user, List<Wish> wishes) {
        final CSVFormat format = CSVFormat.DEFAULT
            .withQuoteMode(QuoteMode.MINIMAL)
            .withDelimiter(';')
            .withHeader(
                messageSource.getMessage("banner", null, Locale.ENGLISH),
                messageSource.getMessage("index", null, Locale.ENGLISH),
                messageSource.getMessage("item", null, Locale.ENGLISH),
                messageSource.getMessage("itemType", null, Locale.ENGLISH),
                messageSource.getMessage("itemRarity", null, Locale.ENGLISH),
                messageSource.getMessage("date", null, Locale.ENGLISH)
            );

        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final PrintWriter printWriter = new PrintWriter(out)
        ) {
            printWriter.write('\ufeff');

            try (CSVPrinter csvPrinter = new CSVPrinter(printWriter, format)) {
                for (Wish wish : wishes) {

                    List<String> data = Arrays.asList(
                        messageSource.getMessage(
                            BannerType.from(wish.getGachaType()).map(BannerType::name).orElse("UNKNOWN"),
                            null, Locale.ENGLISH),
                        String.valueOf(wish.getIndex()),
                        wish.getItem().getName(),
                        messageSource.getMessage(wish.getItem().getItemType(), null, Locale.ENGLISH),
                        String.valueOf(wish.getItem().getRankType()),
                        wish.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', ' ')
                    );

                    csvPrinter.printRecord(data);
                }

                csvPrinter.flush();
                return new ByteArrayInputStream(out.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("failed to import data to CSV file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to import data to CSV file: " + e.getMessage());
        }
    }
}
