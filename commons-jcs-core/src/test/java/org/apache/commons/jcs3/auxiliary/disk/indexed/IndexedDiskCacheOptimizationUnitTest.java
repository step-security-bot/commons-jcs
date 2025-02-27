package org.apache.commons.jcs3.auxiliary.disk.indexed;

import org.apache.commons.jcs3.auxiliary.disk.DiskTestObject;
import org.apache.commons.jcs3.utils.timing.SleepUtil;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.TestCase;

/**
 * Tests for the optimization routine.
 */
public class IndexedDiskCacheOptimizationUnitTest
    extends TestCase
{
    /**
     * Set the optimize at remove count to 10. Add 20. Check the file size. Remove 10. Check the
     * times optimized. Check the file size.
     * @throws Exception
     */
    public void testBasicOptimization()
        throws Exception
    {
        // SETUP
        final int removeCount = 50;

        final IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        cattr.setCacheName( "testOptimization" );
        cattr.setMaxKeySize( removeCount * 2 );
        cattr.setOptimizeAtRemoveCount( removeCount );
        cattr.setDiskPath( "target/test-sandbox/testOptimization" );
        final IndexedDiskCache<Integer, DiskTestObject> disk = new IndexedDiskCache<>( cattr );

        disk.removeAll();

        final int numberToInsert = removeCount * 3;
        final ICacheElement<Integer, DiskTestObject>[] elements = DiskTestObjectUtil
            .createCacheElementsWithTestObjectsOfVariableSizes( numberToInsert, cattr.getCacheName() );

        for (final ICacheElement<Integer, DiskTestObject> element : elements) {
            disk.processUpdate( element );
        }


        Thread.sleep( 1000 );
        final long sizeBeforeRemove = disk.getDataFileSize();
        // System.out.println( "file sizeBeforeRemove " + sizeBeforeRemove );
        // System.out.println( "totalSize inserted " + DiskTestObjectUtil.totalSize( elements, numberToInsert ) );

        // DO WORK
        for ( int i = 0; i < removeCount; i++ )
        {
            disk.processRemove( Integer.valueOf( i ) );
        }

        SleepUtil.sleepAtLeast( 1000 );

        disk.optimizeFile();
        // VERIFY
        final long sizeAfterRemove = disk.getDataFileSize();
        final long expectedSizeAfterRemove = DiskTestObjectUtil.totalSize( elements, removeCount, elements.length );

        // test is prone to failure for timing reasons.
        if ( expectedSizeAfterRemove != sizeAfterRemove )
        {
            SleepUtil.sleepAtLeast( 2000 );
        }

        assertTrue( "The post optimization size should be smaller."
                +"sizeAfterRemove=" + sizeAfterRemove + " sizeBeforeRemove= " +sizeBeforeRemove
                , sizeAfterRemove < sizeBeforeRemove );
        assertEquals( "The file size is not as expected size.", expectedSizeAfterRemove, sizeAfterRemove );
    }
}
