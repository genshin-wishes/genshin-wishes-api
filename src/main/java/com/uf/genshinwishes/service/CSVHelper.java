package com.uf.genshinwishes.service;

import com.uf.genshinwishes.dto.ItemType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CSVHelper {

    public static ByteArrayInputStream wishesToCsv(MessageSource messageSource, User user, List<Wish> wishes) {
        Locale locale = user.getLang() != null
            ? (user.getLang().equals("fr") ? Locale.FRENCH : Locale.ENGLISH)
            : (LocaleContextHolder.getLocale().equals(Locale.FRENCH) ? Locale.FRENCH : Locale.ENGLISH);

        final CSVFormat format = CSVFormat.DEFAULT
            .withQuoteMode(QuoteMode.MINIMAL)
            .withDelimiter(';')
            .withHeader(
                messageSource.getMessage("banner", null, locale),
                messageSource.getMessage("index", null, locale),
                messageSource.getMessage("item", null, locale),
                messageSource.getMessage("itemType", null, locale),
                messageSource.getMessage("itemRarity", null, locale),
                messageSource.getMessage("date", null, locale)
            );

        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final PrintWriter printWriter = new PrintWriter(out);
        ) {
            printWriter.write('\ufeff');

            try (CSVPrinter csvPrinter = new CSVPrinter(printWriter, format)) {
                for (Wish wish : wishes) {

                    List<String> data = Arrays.asList(
                        messageSource.getMessage(BannerType.from(wish.getGachaType()).map(banner -> banner.name()).orElse("UNKNOWN"), null, locale),
                        String.valueOf(wish.getIndex()),
                        locale.equals(Locale.FRENCH) ? wish.getItem().getNameFr() : wish.getItem().getName(),
                        messageSource.getMessage(wish.getItem().getItemType(), null, locale),
                        String.valueOf(wish.getItem().getRankType()),
                        wish.getTime().toInstant().toString()
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
