package com.goosemagnet.connectors.goose.protocol.cs.message;

import com.goosemagnet.connectors.goose.protocol.cs.ByteParser;
import com.goosemagnet.connectors.goose.protocol.cs.Types;
import com.goosemagnet.connectors.goose.protocol.cs.message.ok.OkPacket;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Value
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TextResultSet implements Message {

    List<ColumnDefinition> columns;
    List<String[]> rows;

    @SneakyThrows
    public static TextResultSet fromInputStream(InputStream is) {
        int columnCount = getColumnCount(is);
        List<ColumnDefinition> columns = IntStream.range(0, columnCount)
                .mapToObj(ignored -> parseColumn(is))
                .toList();

        OkPacket intermediateEof = OkPacket.fromInputStream(is);

        List<String[]> rows = new ArrayList<>();
        while (is.available() > 9) { // responseEof is exactly 9 bytes
            String[] row = parseRow(columns.size(), is);
            rows.add(row);
        }

        OkPacket responseEof = OkPacket.fromInputStream(is);

        return TextResultSet.builder()
                .withColumns(columns)
                .withRows(rows)
                .build();
    }

    private static int getColumnCount(InputStream is) {
        Types.Int3 packetLength = ByteParser.parseInt3(is);
        Types.Int1 packetId = ByteParser.parseInt1(is);
        return (int) ByteParser.parseInt1(is).getValue();
    }

    private static ColumnDefinition parseColumn(InputStream is) {
        Types.Int3 packetLength = ByteParser.parseInt3(is);
        Types.Int1 packetId = ByteParser.parseInt1(is);
        return ColumnDefinition.fromInputStream(is);
    }

    private static String parseField(InputStream is) {
        Types.Int1 catalogLength = ByteParser.parseInt1(is);
        return new String(ByteParser.bytes((int) catalogLength.getValue(), is)); // Always "def"
    }

    private static String[] parseRow(int columnCount, InputStream is) {
        Types.Int3 packetLength = ByteParser.parseInt3(is);
        Types.Int1 packetId = ByteParser.parseInt1(is);
        return Stream.generate(() -> parseValue(is)).limit(columnCount).toArray(String[]::new);
    }

    private static String parseValue(InputStream is) {
        Types.Int1 length = ByteParser.parseInt1(is);
        return new String(ByteParser.bytes((int) length.getValue(), is));
    }

    @Value
    @Builder(setterPrefix = "with")
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ColumnDefinition implements Message {
        String catalog;
        String schema;
        String table;
        String originalTable;
        String columnName;
        String originalColumnName;
        Types.Int2 characterSetNumber;
        Types.Int4 columnMaxLength;
        Types.Int1 fieldType;
        Set<ColumnProperty> columnProperties;
        Types.Int1 decimals;

        private static ColumnDefinition fromInputStream(InputStream is) {

            ColumnDefinitionBuilder columnDefinitionBuilder = ColumnDefinition.builder()
                    .withCatalog(parseField(is))
                    .withSchema(parseField(is))
                    .withTable(parseField(is))
                    .withOriginalTable(parseField(is))
                    .withColumnName(parseField(is))
                    .withOriginalColumnName(parseField(is));

            Types.Int1 zeroC = ByteParser.parseInt1(is);// always 0x0C

            ColumnDefinition columnDefinition = columnDefinitionBuilder
                    .withCharacterSetNumber(ByteParser.parseInt2(is))
                    .withColumnMaxLength(ByteParser.parseInt4(is))
                    .withFieldType(ByteParser.parseInt1(is))
                    .withColumnProperties(ColumnProperty.from(ByteParser.parseInt2(is).getValue()))
                    .withDecimals(ByteParser.parseInt1(is))
                    .build();

            ByteParser.bytes(2, is);

            return columnDefinition;
        }
    }
}
