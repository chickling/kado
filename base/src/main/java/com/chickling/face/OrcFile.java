package com.chickling.face;

import java.io.ByteArrayInputStream;

/**
 * Created by gl08 on 2017/2/24.
 */
public abstract class OrcFile  {

    public  abstract ByteArrayInputStream  getInputStream(String path,int start,int end);

}
