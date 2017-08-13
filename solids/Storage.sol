pragma solidity ^0.4.13;


contract Storage {
    string public version = "0.0.5";

    /* Holds properties (maps name to property) */
    mapping (string => Property) properties;

    /* Hold permissions (maps address to permission level) */
    mapping (address => uint) permissions;

    /* Event that posts address of sender and message (defined upon instantiation) */
    event Note(address addr, string message);

    /* Holds file data (mapping stores file in bytes32 chunks) */
    struct File {
    mapping (uint => bytes32) data;
    }

    /* Defines a property (can hold files and metadata) */
    struct Property {
    /*TODO: limit access to owner or permission level*/
    /* Stores property's files */
    mapping (string => File) files;

    /* Stores property's metadata */
    mapping (string => string) metadata;
    }

    /* Constructor */
    function Storage() {
        /* Creator is given maximum permissions */
        permissions[msg.sender] = 100;
    }

    function uploadFile(string _propertyName, string _fileName, bytes32 _data, uint count) returns (bool) {
        /* Check permissions */
        checkPerms(2, "You do not have permission to add files");
        /* Perform action */
        properties[_propertyName].files[_fileName].data[count] = _data;
        Note(msg.sender, "File uploaded successfully");
        return true;
    }

    function addFile(string _propertyName, string _fileName) returns (bool) {
        checkPerms(2, "You do not have permission to add files");
        properties[_propertyName].files[_fileName] = File();
        Note(msg.sender, "File created successfully");
        return true;
    }

    function addProperty(string _propertyName) returns (bool) {
        checkPerms(2, "You do not have permission to add files");
        properties[_propertyName] = Property();
        Note(msg.sender, "Property created successfully");
        return true;
    }

    function addPropertyMetadata(string _propertyName, string _key, string _value) returns (bool) {
        checkPerms(2, "You do not have permission to add property metadata");
        properties[_propertyName].metadata[_key] = _value;
        Note(msg.sender, "Property metadata added successfully");
        return true;
    }

    function getPropertyMetadata(string _propertyName, string _key) constant returns (string) {
        checkPerms(1, "You do not have permission to get property metadata");
        return properties[_propertyName].metadata[_key];
    }

    function getFile(string _propertyName, string _fileName, uint count) constant returns (bytes32) {
        checkPerms(1, "You do not have permission to get files");
        return properties[_propertyName].files[_fileName].data[count];
    }

    function addAccount(address addr, uint perms) returns (bool) {
        checkPerms(100, "You do not have permission to add accounts");
        permissions[addr] = perms;
        return true;
    }

    function destroy() returns (bool) {
        checkPerms(100, "You do not have permission to destroy this contract");
        Note(msg.sender, "Self destructing... goodbye, old friend...");
        selfdestruct(msg.sender);
    }

    function checkPerms(uint requiredPerms, string message) returns (bool) {
        if (permissions[msg.sender] == 0x0 || permissions[msg.sender] < requiredPerms) {
            Note(msg.sender, message);
            return false;
        }
        return true;
    }
}
