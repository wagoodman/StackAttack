package com.wagoodman.stackattack;

public class Identity extends Object
{
	private String id;
	private String groupId = null;
	
	public Identity() { id = BaseConverterUtil.Base62Random(); }
	
	public void setId(String newId) { id = newId; }
	public void setGroupId(String gid) { groupId = gid; }
	public void removeGroupMembership() {groupId = null; }
	
	public String getId() { return id; }
	public String getGroupId() { return groupId; }
	
	public Boolean isInGroup() { return groupId!=null; }
	
	public Integer tolkenizeId() { return BaseConverterUtil.tolkenizeStringToInt(id); }
	
}
