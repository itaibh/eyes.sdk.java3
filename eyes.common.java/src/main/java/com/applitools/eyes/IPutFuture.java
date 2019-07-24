package com.applitools.eyes;

import com.applitools.eyes.visualgrid.model.RGridResource;

import java.util.concurrent.Future;

public interface IPutFuture extends Future{
    Future getPutFuture();

    RGridResource getResource();
}
