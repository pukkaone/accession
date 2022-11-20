package com.github.pukkaone.accession.persistence;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Converts code to enum constant.
 *
 * @param <C>
 *     code representation type
 * @param <E>
 *     enum type, must implement {@link Codeable} interface
 */
public class CodeToEnumMapper<C, E extends Enum<E> & Codeable<C>> {

  private E invalid;
  private final Map<C, E> codeToEnumMap = new LinkedHashMap<>();

  /**
   * Constructor.
   *
   * @param enumClass
   *     enum class
   */
  public CodeToEnumMapper(Class<E> enumClass) {
    for (E e : enumClass.getEnumConstants()) {
      if (e.name().equals("INVALID")) {
        invalid = e;
        continue;
      }

      C normalizedCode = normalizeString(e.getCode());
      boolean duplicate = codeToEnumMap.put(normalizedCode, e) != null;
      if (duplicate) {
        throw new IllegalArgumentException(String.format(
            "Duplicate code [%s] found trying to map enum constant [%s]",
            e.getCode(),
            e));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private C normalizeString(C code) {
    return (code instanceof String)
        ? (C) ((String) code).trim().toUpperCase(Locale.ENGLISH)
        : code;
  }

  /**
   * Converts code to enum constant. This method has special case logic when the enum has a constant
   * named {@code INVALID}. In that case, if the input code is unknown, then the method returns
   * {@code INVALID}. If there is no enum constant named {@code INVALID} and the input code is
   * unknown, then the method returns empty.
   *
   * @param code
   *     input code
   * @return optional enum constant; empty if input code is null
   */
  public Optional<E> fromCode(C code) {
    if (code == null) {
      return Optional.empty();
    }

    C normalizedCode = normalizeString(code);
    return Optional.ofNullable(codeToEnumMap.getOrDefault(normalizedCode, invalid));
  }

  /**
   * Gets valid codes.
   *
   * @return comma-separated list
   */
  public String getValidCodes() {
    StringJoiner joiner = new StringJoiner(", ");
    codeToEnumMap.keySet()
        .forEach(code -> joiner.add(code.toString()));
    return joiner.toString();
  }

  /**
   * Checks if enum constant is valid.
   *
   * @param member
   *     enum constant to check
   * @return true if enum constant is valid
   */
  public boolean isValid(E member) {
    return member != invalid;
  }
}
