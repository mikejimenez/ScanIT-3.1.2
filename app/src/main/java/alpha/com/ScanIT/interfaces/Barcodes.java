package alpha.com.ScanIT.interfaces;

public class Barcodes {

    private int _id;
    private String _Barcode;
    private String _Company;
    private String _Name;
    private String _Department;
    private String _Username;
    private String _ListView;
    private String _Count;

    public Barcodes() {

    }

    public Barcodes(int id, String Barcode, String _Company, String _Name, String _Department, String _Username, String _ListView, String _Count) {
        this._id = id;
        this._Barcode = Barcode;
        this._Company = _Company;
        this._Name = _Name;
        this._Department = _Department;
        this._Username = _Username;
        this._ListView = _ListView;
        this._Count = _Count;
    }

    public Barcodes(String Barcode, String _Company, String _Name, String _Department, String _Username, String _ListView, String _Count) {
        this._Barcode = Barcode;
        this._Company = _Company;
        this._Name = _Name;
        this._Department = _Department;
        this._Username = _Username;
        this._ListView = _ListView;
        this._Count = _Count;

    }

    // setting id
    public void setID(int id) {
        this._id = id;
    }

    // setting Barcode
    public void setBarcode(String Barcode) {
        this._Barcode = Barcode;
    }

    // getting Barcode
    public String getBarcode() {
        return this._Barcode;
    }

    // setting Company
    public void setCompany(String Company) {
        this._Company = Company;
    }

    // getting Company
    public String getCompany() {
        return this._Company;
    }

    // setting Department
    public void setDepartment(String Department) {this._Department = Department; }

    // getting Department
    public String getDepartment() { return this._Department; }

    public void setUsername(String Username) { this._Username = Username; }

    public String getUsername() { return this._Username; }

    public void setName (String Name) { this._Name = Name; }

    public String getName() { return  this._Name; }

    public void setListView (String ListView) { this._ListView = ListView; }

    public String getListView () { return this._ListView; }

    public void setCount(String Count) { this._Count = Count; }

    public String getCount () { return this._Count; }

}
