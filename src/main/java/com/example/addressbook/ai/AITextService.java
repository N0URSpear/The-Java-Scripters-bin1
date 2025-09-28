// AITextService.java
package com.example.addressbook.ai;

public interface AITextService {
    String generatePassage(String topic, int targetWords,
                           boolean includeUpper, boolean includeNumbers,
                           boolean includePunct, boolean includeSpecial) throws Exception;
}
