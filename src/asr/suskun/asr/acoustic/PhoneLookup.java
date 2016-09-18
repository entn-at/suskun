package suskun.asr.acoustic;

import suskun.core.collections.BidirectionalIndexLookup;
import suskun.core.collections.UIntValueMap;

import java.io.IOException;
import java.nio.file.Path;

public class PhoneLookup {
    UIntValueMap<Phone> indexLookup = new UIntValueMap<>();
    UIntValueMap<String> idIndexLookup = new UIntValueMap<>();
    Phone[] phoneLookup;

    private PhoneLookup(UIntValueMap<Phone> indexLookup, Phone[] phoneLookup) {
        this.indexLookup = indexLookup;
        this.phoneLookup = phoneLookup;
        for (Phone phone : indexLookup) {
            idIndexLookup.put(phone.id, indexLookup.get(phone));
        }
    }

    public static PhoneLookup loadFromFile(Path path) throws IOException {
        BidirectionalIndexLookup<String> lookup = BidirectionalIndexLookup.fromTextFileWithIndex(path);
        UIntValueMap<Phone> indexLookup = new UIntValueMap<>();
        Phone[] phoneLookup = new Phone[lookup.size()];
        for (String key : lookup.keys()) {
            boolean filler = key.contains("+") || key.contains("%");
            boolean silence = key.equalsIgnoreCase("SIL");
            boolean nonSpeech = filler || silence || key.contains("<") || key.contains("#");
            int index = lookup.getIndex(key);
            Phone phone = new Phone(key, index, silence, filler, nonSpeech);
            indexLookup.put(phone, index);
            phoneLookup[index] = phone;
        }
        return new PhoneLookup(indexLookup, phoneLookup);
    }

    public Phone getPhone(int index) {
        return phoneLookup[index];
    }

    public int getIndex(Phone phone) {
        return indexLookup.get(phone);
    }

    public int getIndex(String phoneId) {
        return idIndexLookup.get(phoneId);
    }


}
