# Daysie

[![Maven Central](https://img.shields.io/maven-central/v/net.vanfleteren.daysie/daysie-parent.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=net.vanfleteren.daysie)

Daysie is a natural language date and time parser for Java. It allows you to parse human-readable date expressions into structured date and time objects. It is built on top of the JParsec library and supports multiple languages.

## Features

* Parse relative dates like "today", "yesterday", "last week", or "3 days ago".
* Parse absolute dates and ranges like "2026-02-15", "2026-W07", or "2026-01-01 to 2026-02-01".
* Support for time expressions in both 24h and 12h formats.
* Complex range expressions using operators like "before", "after", "since", "until", and "between".
* Multi-language support (English and Dutch included by default).
* Extensible keyword configuration via the LanguageKeywords record.

## Installation

Daysie is a Maven-based project. You can include it in your project by building it locally or adding it as a dependency if available in your repository.

```xml
<dependency>
    <groupId>net.vanfleteren.daysie</groupId>
    <artifactId>daysie-core</artifactId>
    <version><!-- latest-version --></version>
</dependency>
```

## Usage

The primary entry point is the DateValueParser class.

### Simple Parsing

```java
import net.vanfleteren.daysie.core.DateValue;
import net.vanfleteren.daysie.core.DateValueParser;

DateValueParser parser = new DateValueParser();
DateValue result = parser.parser().parse("last week");
System.out.println(result);
```

### Multi-language Support

You can combine multiple languages or create your own:

```java
import net.vanfleteren.daysie.core.LanguageKeywords;
import java.util.List;

LanguageKeywords combined = LanguageKeywords.combine(List.of(
    LanguageKeywords.ENGLISH, 
    LanguageKeywords.DUTCH
));
DateValueParser parser = new DateValueParser(combined);
DateValue result = parser.parser().parse("vorige week");
```

### Supported Expressions

* Absolute: 2026-02-15, 2026-02, 2026-W07
* Relative: today, yesterday, tomorrow, now
* Durations: last 3 days, next 2 weeks, this month
* Points in time: start of last month, end of this year, 5 minutes ago
* Operators: after 2026-01-01, since monday 08:00, between 5 minutes ago and now
* And many more, check the tests for more examples!

## License

This project is licensed under the MIT License. See the LICENSE file for details.
