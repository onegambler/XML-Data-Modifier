package com.onegambler.dataremover.core;

import com.google.common.base.Strings;

import static java.util.Objects.requireNonNull;

public class ContentReplaceRule extends ContentRule {

    private final String regex;
    private final String replacement;

    public ContentReplaceRule(String regex, String replacement) {
        requireNonNull(regex, "Regex string cannot be null");
        requireNonNull(replacement, "Replacement string cannot be null");
        this.regex = regex;
        this.replacement = replacement;
    }

    private static final String STAR_REGEX = "*";

    @Override
    public String elaborate(String content) {
        if (STAR_REGEX.equals(regex)) {
            return replacement;
        }

        return Strings.nullToEmpty(content).replaceAll(regex, replacement);
    }

    @Override
    public String toString() {
        return "ContentReplaceRule{" + "regex='" + regex + '\'' + ", replacement='" + replacement + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentReplaceRule that = (ContentReplaceRule) o;

        if (regex != null ? !regex.equals(that.regex) : that.regex != null) return false;
        return !(replacement != null ? !replacement.equals(that.replacement) : that.replacement != null);

    }

    @Override
    public int hashCode() {
        int result = regex != null ? regex.hashCode() : 0;
        result = 31 * result + (replacement != null ? replacement.hashCode() : 0);
        return result;
    }
}
