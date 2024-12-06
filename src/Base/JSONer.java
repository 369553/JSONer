/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Base;

import RWServices.RWService;
import java.io.File;

/**
 *
 * @author Yazılım alanı
 */
public class JSONer {
    public static void main(String[] args){
        System.out.println(RWService.getService().readDataAsText(new File("od.txt")));
    }
}
