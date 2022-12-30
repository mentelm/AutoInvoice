package pl.mentelm.autoinvoice;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class BinaryData {
    String filename;
    String type;
    byte[] content;
}
