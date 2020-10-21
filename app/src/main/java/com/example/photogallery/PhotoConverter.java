package com.example.photogallery;

import java.io.File;
import java.io.IOException;

public interface PhotoConverter <T, V> {

    public T create(V source);

}
