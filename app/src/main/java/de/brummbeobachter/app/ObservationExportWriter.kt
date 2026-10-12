package de.brummbeobachter.app

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ObservationExportWriter {

    private val headers =
        listOf(
            "observer_id",
            "event_id",
            "scheduled_time_utc",
            "scheduled_time_local",
            "response_time_utc",
            "response_time_local",
            "rating",
            "comment",
            "latitude",
            "longitude",
            "app_version",
            "wav_present",
            "wav_path"
        )

    fun toCsv(
        rows: List<OwnObservation>
    ): String {
        val allRows =
            buildList {
                add(headers)
                rows.forEach {
                    add(exportRow(it))
                }
            }

        return allRows.joinToString(
            separator = "\r\n",
            postfix = "\r\n"
        ) { row ->
            row.joinToString(";") {
                csvEscape(it)
            }
        }
    }

    fun writeCsv(
        output: OutputStream,
        rows: List<OwnObservation>
    ) {
        output.write(
            "\uFEFF".plus(
                toCsv(rows)
            )
                .toByteArray(
                    StandardCharsets.UTF_8
                )
        )
    }

    fun toXlsxBytes(
        rows: List<OwnObservation>
    ): ByteArray {
        val output =
            ByteArrayOutputStream()

        writeXlsx(
            output,
            rows
        )

        return output.toByteArray()
    }

    fun writeXlsx(
        output: OutputStream,
        rows: List<OwnObservation>
    ) {
        ZipOutputStream(output).use { zip ->
            putText(
                zip,
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
            )

            putText(
                zip,
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
            )

            putText(
                zip,
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Beobachtungen" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""
            )

            putText(
                zip,
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            )

            putText(
                zip,
                "xl/styles.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="2">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><name val="Calibri"/></font>
</fonts>
<fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
<borders count="1"><border/></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="2">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
</cellXfs>
</styleSheet>"""
            )

            putText(
                zip,
                "xl/worksheets/sheet1.xml",
                sheetXml(rows)
            )
        }
    }

    private fun sheetXml(
        rows: List<OwnObservation>
    ): String =
        buildString {
            append(
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>"""
            )
            append(
                """<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">"""
            )
            append(
                """<sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>"""
            )
            append("<sheetData>")

            appendRow(
                this,
                1,
                headers,
                header = true
            )

            rows.forEachIndexed {
                    index,
                    observation ->
                appendRow(
                    this,
                    index + 2,
                    exportRow(observation),
                    header = false
                )
            }

            append("</sheetData>")
            append("</worksheet>")
        }

    private fun appendRow(
        out: StringBuilder,
        rowNumber: Int,
        values: List<String>,
        header: Boolean
    ) {
        out.append(
            "<row r=\"$rowNumber\">"
        )

        values.forEachIndexed {
                columnIndex,
                value ->
            val ref =
                columnName(
                    columnIndex + 1
                ) + rowNumber

            out.append(
                "<c r=\"$ref\" t=\"inlineStr\"" +
                    if (header) {
                        " s=\"1\""
                    } else {
                        ""
                    } +
                    "><is><t xml:space=\"preserve\">" +
                    xmlEscape(value) +
                    "</t></is></c>"
            )
        }

        out.append("</row>")
    }

    private fun exportRow(
        item: OwnObservation
    ): List<String> =
        listOf(
            item.observerId,
            item.eventId,
            item.scheduledTime,
            localTime(item.scheduledTime),
            item.responseTime.orEmpty(),
            item.responseTime
                ?.let(::localTime)
                .orEmpty(),
            item.rating.toString(),
            item.comment.orEmpty(),
            item.latitude.toString(),
            item.longitude.toString(),
            item.appVersion,
            item.wavPresent.toString(),
            item.wavPath.orEmpty()
        )

    private fun localTime(
        value: String
    ): String =
        runCatching {
            OffsetDateTime
                .parse(value)
                .atZoneSameInstant(
                    ZoneId.systemDefault()
                )
                .format(
                    DateTimeFormatter
                        .ISO_OFFSET_DATE_TIME
                )
        }.getOrDefault(value)

    private fun csvEscape(
        value: String
    ): String {
        val escaped =
            value.replace(
                "\"",
                "\"\""
            )

        return if (
            escaped.contains(';') ||
            escaped.contains('"') ||
            escaped.contains('\n') ||
            escaped.contains('\r')
        ) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun xmlEscape(
        value: String
    ): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun columnName(
        oneBased: Int
    ): String {
        var number =
            oneBased
        val result =
            StringBuilder()

        while (number > 0) {
            number--
            result.append(
                ('A'.code +
                    number % 26).toChar()
            )
            number /= 26
        }

        return result
            .reverse()
            .toString()
    }

    private fun putText(
        zip: ZipOutputStream,
        name: String,
        text: String
    ) {
        zip.putNextEntry(
            ZipEntry(name)
        )
        zip.write(
            text.toByteArray(
                StandardCharsets.UTF_8
            )
        )
        zip.closeEntry()
    }
}
