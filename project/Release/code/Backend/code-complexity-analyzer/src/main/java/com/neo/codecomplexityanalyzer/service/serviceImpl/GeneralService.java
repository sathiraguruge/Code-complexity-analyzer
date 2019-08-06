/*
-------------------------------------------------------------------------------------------------------
--  Date        Sign    History
--  ----------  ------  --------------------------------------------------------------------------------
--  2019-08-06  Sathira  185817, Created General Service Interface.
--  ----------  ------  --------------------------------------------------------------------------------
*/

package com.neo.codecomplexityanalyzer.service.serviceImpl;

import java.util.ArrayList;

public interface GeneralService {
    public Long getSourceCodeLength();
    public String getSourceCode(String path);
    public ArrayList<String> getSourceCodeLinesUpto(int lineNumber);
    public ArrayList<String> getSourceCodeLine(int lineNumber);
    public int countOccurences(String str, String word);
    public int getLogicalCount(String subString);
}